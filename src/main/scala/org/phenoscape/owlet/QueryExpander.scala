package org.phenoscape.owlet

import scala.collection.JavaConversions._
import scala.collection.Map
import scala.collection.Set
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import com.hp.hpl.jena.graph.NodeFactory
import com.hp.hpl.jena.graph.Node_Literal
import com.hp.hpl.jena.graph.Node_Variable
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.sparql.expr.E_OneOf
import com.hp.hpl.jena.sparql.expr.ExprList
import com.hp.hpl.jena.sparql.expr.ExprVar
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode
import com.hp.hpl.jena.sparql.syntax.ElementFilter
import com.hp.hpl.jena.sparql.syntax.ElementGroup
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase
import com.hp.hpl.jena.sparql.syntax.ElementWalker
import com.hp.hpl.jena.query.Syntax
import com.hp.hpl.jena.graph.Node_URI

/**
 * Processes SPARQL queries containing triple patterns with embedded OWL class expressions.
 * Class expressions are evaluated and queried against an OWL reasoner, and the triple
 * pattern is replaced with a FILTER(?x IN (...)).
 */
class QueryExpander(reasoner: OWLReasoner) {

  private val factory = OWLManager.getOWLDataFactory();
  val SUBCLASS_OF = OWLRDFVocabulary.RDFS_SUBCLASS_OF.getIRI().toString();
  val EQUIVALENT_CLASS = OWLRDFVocabulary.OWL_EQUIVALENT_CLASS.getIRI().toString();
  val TYPE = OWLRDFVocabulary.RDF_TYPE.getIRI().toString();
  val OWLET_NS = "http://purl.org/phenoscape/owlet/syntax#";
  val MANCHESTER = OWLET_NS + "omn";
  val OWLXML = OWLET_NS + "owx";
  val FUNCTIONAL = OWLET_NS + "ofn";
  val SYNTAXES = Set(MANCHESTER, OWLXML, FUNCTIONAL);

  def expandQueryString(query: String): String = {
    val parsedQuery = QueryFactory.create(query);
    return expandQuery(parsedQuery).toString();
  }

  def expandQuery(query: Query): Query = {
    val prefixMap = query.getPrefixMapping().getNsPrefixMap();
    ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase() {
      override def visit(group: ElementGroup): Unit = {
        processElementGroup(group, prefixMap);
      }
    });
    return query;
  }

  private def processElementGroup(group: ElementGroup, prefixes: Map[String, String]): Unit = {
    for (pathBlock <- group.getElements().filter(_.isInstanceOf[ElementPathBlock]).map(_.asInstanceOf[ElementPathBlock])) {
      val patterns = pathBlock.getPattern().iterator();
      for (pattern <- patterns) {
        val predicateURI = Option(pattern.getPredicate()).collect({ case uri: Node_URI => uri.getURI() }).getOrElse(null) //predicate is null if property path
        val filterOpt = (pattern.getSubject(), predicateURI, pattern.getObject()) match {
          case (variable: Node_Variable,
            SUBCLASS_OF,
            expression: Node_Literal) if SYNTAXES(expression.getLiteralDatatypeURI()) =>
            runQueryAndMakeFilter(querySubClasses, expression, prefixes, variable);
          case (expression: Node_Literal,
            SUBCLASS_OF,
            variable: Node_Variable) if SYNTAXES(expression.getLiteralDatatypeURI()) =>
            runQueryAndMakeFilter(querySuperClasses, expression, prefixes, variable);
          case (variable: Node_Variable,
            EQUIVALENT_CLASS,
            expression: Node_Literal) if SYNTAXES(expression.getLiteralDatatypeURI()) =>
            runQueryAndMakeFilter(queryEquivalentClasses, expression, prefixes, variable);
          case (expression: Node_Literal,
            EQUIVALENT_CLASS,
            variable: Node_Variable) if SYNTAXES(expression.getLiteralDatatypeURI()) =>
            runQueryAndMakeFilter(queryEquivalentClasses, expression, prefixes, variable);
          case (variable: Node_Variable,
            TYPE,
            expression: Node_Literal) if SYNTAXES(expression.getLiteralDatatypeURI()) =>
            runQueryAndMakeFilter(queryIndividuals, expression, prefixes, variable);
          case _ => None;
        }
        for (filter <- filterOpt) {
          patterns.remove();
          group.addElement(filter);
        }
      }
    }
  }

  private def runQueryAndMakeFilter(queryFunction: (OWLClassExpression => Set[_ <: OWLEntity]), classExpression: Node_Literal, prefixes: Map[String, String], variable: Node_Variable): Option[ElementFilter] = {
    val parsedExpression = parseExpression(classExpression, prefixes);
    parsedExpression match {
      case Some(expression) => Option(makeFilter(variable, queryFunction(expression)));
      case None => None;
    }
  }

  def makeFilter(variable: Node_Variable, classes: Iterable[OWLEntity]): ElementFilter = {
    val nodes = classes.map(term => new NodeValueNode(NodeFactory.createURI(term.getIRI().toString())));
    val oneOf = new E_OneOf(new ExprVar(variable), new ExprList(nodes.toList));
    new ElementFilter(oneOf);
  }

  def parseExpression(literal: Node_Literal, prefixes: Map[String, String]): Option[OWLClassExpression] = {
    val expression = literal.getLiteralLexicalForm();
    literal.getLiteralDatatypeURI() match {
      case MANCHESTER => ManchesterSyntaxClassExpressionParser.parse(expression, prefixes);
      case OWLXML => OWLXMLClassExpressionParser.parse(expression, prefixes);
      case FUNCTIONAL => parseFunctional(expression, prefixes);
    }
  }

  private def parseFunctional(expression: String, prefixes: Map[String, String]): Option[OWLClassExpression] = {
    return None; //TODO
  }

  private def querySubClasses(expression: OWLClassExpression): Set[OWLClass] = {
    val subclasses = reasoner.getSubClasses(expression, false).getFlattened()
    if (!expression.isAnonymous()) {
      subclasses + expression.asOWLClass()
    } else {
      subclasses
    }

  }

  private def queryEquivalentClasses(expression: OWLClassExpression): Set[OWLClass] = {
    val equivalents = reasoner.getEquivalentClasses(expression).getEntities()
    if (!expression.isAnonymous()) {
      equivalents + expression.asOWLClass()
    } else {
      equivalents
    }
  }

  private def querySuperClasses(expression: OWLClassExpression): Set[OWLClass] = {
    val superclasses = reasoner.getSuperClasses(expression, false).getFlattened()
    if (!expression.isAnonymous()) {
      superclasses + expression.asOWLClass()
    } else {
      superclasses
    }
  }

  private def queryIndividuals(expression: OWLClassExpression): Set[OWLNamedIndividual] = {
    reasoner.getInstances(expression, false).getFlattened();
  }

}