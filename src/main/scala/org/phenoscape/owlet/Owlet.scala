package org.phenoscape.owlet

import java.util.UUID

import scala.collection.JavaConversions._
import scala.collection.Map
import scala.collection.immutable.Set

import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Node_Literal
import org.apache.jena.graph.Node_Variable
import org.apache.jena.graph.Triple
import org.apache.jena.graph.impl.GraphBase
import org.apache.jena.query.ARQ
import org.apache.jena.query.Query
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.query.ResultSet
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.core.TriplePath
import org.apache.jena.sparql.expr.E_OneOf
import org.apache.jena.sparql.expr.ExprFunctionOp
import org.apache.jena.sparql.expr.ExprList
import org.apache.jena.sparql.expr.ExprVar
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode
import org.apache.jena.sparql.syntax.ElementFilter
import org.apache.jena.sparql.syntax.ElementGroup
import org.apache.jena.sparql.syntax.ElementPathBlock
import org.apache.jena.sparql.syntax.ElementVisitorBase
import org.apache.jena.sparql.syntax.ElementWalker
import org.apache.jena.sparql.syntax.RecursiveElementVisitor
import org.apache.jena.util.iterator.ExtendedIterator
import org.apache.jena.util.iterator.WrappedIterator
import org.apache.jena.vocabulary.OWL2
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.reasoner.OWLReasoner

/**
 * Processes SPARQL queries containing triple patterns with embedded OWL class expressions.
 * Class expressions are evaluated and queried against an OWL reasoner, and the triple
 * pattern is replaced with a FILTER(?x IN (...)).
 */
class Owlet(reasoner: OWLReasoner) {

  private lazy val factory = OWLManager.getOWLDataFactory

  def expandQueryString(query: String): String = {
    val parsedQuery = QueryFactory.create(query)
    expandQuery(parsedQuery).toString
  }

  def expandQuery(query: Query): Query = {
    val prefixMap = query.getPrefixMapping.getNsPrefixMap
    ElementWalker.walk(query.getQueryPattern, new SPARQLVisitor(prefixMap))
    query
  }

  private class SPARQLVisitor(prefixes: Map[String, String]) extends RecursiveElementVisitor(new ElementVisitorBase()) {

    override def endElement(filter: ElementFilter): Unit = filter.getExpr match {
      case existsLike: ExprFunctionOp => ElementWalker.walk(existsLike.getElement, this)
      case _                          => Unit
    }

    override def endElement(group: ElementGroup): Unit = {
      for (pathBlock <- group.getElements.collect({ case pb: ElementPathBlock => pb })) {
        val patterns = pathBlock.getPattern.iterator
        for {
          pattern <- patterns
          result <- matchTriple(pattern, prefixes)
          filter <- result.toFilter
        } {
          patterns.remove()
          group.addElement(filter)
        }
      }
    }

  }

  private def querySubClasses(expression: OWLClassExpression): Set[OWLClass] = {
    val subclasses = reasoner.getSubClasses(expression, false).getFlattened.toSet
    if (!expression.isAnonymous)
      subclasses + expression.asOWLClass
    else
      subclasses
  }

  private def queryEquivalentClasses(expression: OWLClassExpression): Set[OWLClass] = {
    val equivalents = reasoner.getEquivalentClasses(expression).getEntities.toSet
    if (!expression.isAnonymous)
      equivalents + expression.asOWLClass
    else
      equivalents
  }

  private def querySuperClasses(expression: OWLClassExpression): Set[OWLClass] = {
    val superclasses = reasoner.getSuperClasses(expression, false).getFlattened.toSet
    if (!expression.isAnonymous)
      superclasses + expression.asOWLClass
    else
      superclasses
  }

  private def queryIndividuals(expression: OWLClassExpression): Set[OWLNamedIndividual] = reasoner.getInstances(expression, false).getFlattened.toSet

  private def matchTriple(triple: TriplePath, prefixes: Map[String, String]): Option[OwletResult] = for {
    (expression, queryFunction) <- (triple.getSubject, triple.getPredicate, triple.getObject) match {
      case (_: Node_Variable | Node.ANY, Owlet.SubClassOf, expression: Node_Literal) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI) =>
        Option(expression, querySubClasses _)
      case (expression: Node_Literal, Owlet.SubClassOf, _: Node_Variable | Node.ANY) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI) =>
        Option(expression, querySuperClasses _)
      case (_: Node_Variable | Node.ANY, Owlet.EquivalentClass, expression: Node_Literal) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI) =>
        Option(expression, queryEquivalentClasses _)
      case (expression: Node_Literal, Owlet.EquivalentClass, _: Node_Variable | Node.ANY) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI) =>
        Option(expression, queryEquivalentClasses _)
      case (_: Node_Variable | Node.ANY, Owlet.Type, expression: Node_Literal) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI) =>
        Option(expression, queryIndividuals _)
      case _ => None
    }
    classExpression <- Owlet.parseExpression(expression, prefixes)
  } yield {
    val namedQuery = addQueryAsClass(classExpression)
    OwletResult(triple.asTriple, queryFunction(namedQuery))
  }

  def addQueryAsClass(expression: OWLClassExpression): OWLClass = expression match {
    case named: OWLClass => named
    case anonymous => {
      val ontology = reasoner.getRootOntology
      val manager = ontology.getOWLOntologyManager
      val namedQuery = factory.getOWLClass(IRI.create(s"http://example.org/${UUID.randomUUID.toString}"))
      manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(namedQuery, expression))
      reasoner.flush()
      namedQuery
    }
  }

  def performSPARQLQuery(query: Query): ResultSet = {
    val prefixMap = query.getPrefixMapping.getNsPrefixMap.toMap
    val model = ModelFactory.createModelForGraph(new OwletGraph(prefixMap))
    val queryExecution = QueryExecutionFactory.create(query, model)
    // The query optimizer must be turned off so that the OWL reasoner 
    // can be queried once rather than for every VALUES binding.
    // This also makes property paths work correctly with our fake RDF graph.
    ARQ.enableOptimizer(queryExecution.getContext, false)
    queryExecution.execSelect
  }

  private class OwletGraph(prefixes: Map[String, String]) extends GraphBase {

    override def graphBaseFind(pattern: Triple): ExtendedIterator[Triple] = {
      val results = matchTriple(new TriplePath(pattern), prefixes).map(_.toTriples).getOrElse(Set())
      WrappedIterator.create(results.iterator)
    }

  }

}

object Owlet {

  val SubClassOf = RDFS.Nodes.subClassOf
  val EquivalentClass = OWL2.equivalentClass.asNode
  val Type = RDF.Nodes.`type`
  val OWLET_NS = "http://purl.org/phenoscape/owlet/syntax#"
  val MANCHESTER = OWLET_NS + "omn"
  val OWLXML = OWLET_NS + "owx"
  val FUNCTIONAL = OWLET_NS + "ofn"
  val SYNTAXES = Set(MANCHESTER, OWLXML, FUNCTIONAL)

  def runQueryAndMakeFilter(queryFunction: (OWLClassExpression => Set[_ <: OWLEntity]), classExpression: Node_Literal, prefixes: Map[String, String], variable: Node_Variable): Option[ElementFilter] = {
    val parsedExpression = parseExpression(classExpression, prefixes)
    parsedExpression match {
      case Some(expression) => Option(makeFilter(variable, queryFunction(expression)))
      case None             => None
    }
  }

  private def parseExpression(literal: Node_Literal, prefixes: Map[String, String]): Option[OWLClassExpression] = {
    val expression = literal.getLiteralLexicalForm
    literal.getLiteralDatatypeURI match {
      case MANCHESTER => ManchesterSyntaxClassExpressionParser.parse(expression, prefixes).toOption
      case OWLXML     => OWLXMLClassExpressionParser.parse(expression, prefixes)
      case FUNCTIONAL => parseFunctional(expression, prefixes)
    }
  }

  private def parseFunctional(expression: String, prefixes: Map[String, String]): Option[OWLClassExpression] = ???

  def makeFilter(variable: Node_Variable, classes: Iterable[OWLEntity]): ElementFilter = {
    val nodes = classes.map(term => new NodeValueNode(NodeFactory.createURI(term.getIRI.toString)))
    val oneOf = new E_OneOf(new ExprVar(variable), new ExprList(nodes.toList))
    new ElementFilter(oneOf)
  }

}