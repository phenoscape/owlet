package org.phenoscape.owlet

import org.apache.jena.graph._
import org.apache.jena.query._
import org.apache.jena.sparql.core.{TriplePath, Var}
import org.apache.jena.sparql.engine.binding.BindingFactory
import org.apache.jena.sparql.expr._
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode
import org.apache.jena.sparql.syntax._
import org.apache.jena.vocabulary.{OWL2, RDF, RDFS}
import org.eclipse.rdf4j.model.vocabulary.SESAME
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.reasoner.OWLReasoner

import scala.collection.Map
import scala.jdk.CollectionConverters._

/**
 * Processes SPARQL queries containing triple patterns with embedded OWL class expressions.
 * Class expressions are evaluated and queried against an OWL reasoner, and the triple
 * pattern is replaced with a FILTER(?x IN (...)).
 */
class Owlet(reasoner: OWLReasoner) {

  def expandQueryString(query: String, asValues: Boolean = false): String = {
    val parsedQuery = QueryFactory.create(query)
    expandQuery(parsedQuery, asValues).toString
  }

  def expandQuery(query: Query, asValues: Boolean = false): Query = {
    val newQuery = query.cloneQuery()
    val prefixMap = newQuery.getPrefixMapping.getNsPrefixMap
    ElementWalker.walk(newQuery.getQueryPattern, new SPARQLVisitor(prefixMap.asScala, asValues))
    newQuery
  }

  private class SPARQLVisitor(prefixes: Map[String, String], asValues: Boolean) extends ElementVisitorBase {

    override def visit(filter: ElementFilter): Unit = filter.getExpr match {
      case existsLike: ExprFunctionOp => existsLike.getElement.visit(this)
      case _                          => ()
    }

    override def visit(subquery: ElementSubQuery): Unit = subquery.getQuery.getQueryPattern.visit(this)

    override def visit(group: ElementGroup): Unit = {
      for (pathBlock <- group.getElements.asScala.collect({ case pb: ElementPathBlock => pb })) {
        val patterns = pathBlock.getPattern.iterator
        while (patterns.hasNext) {
          val pattern = patterns.next()
          for {
            result <- matchTriple(pattern, prefixes)
            filter <- if (asValues) result.toValues else result.toFilter
          } {
            patterns.remove()
            group.addElement(filter)
          }
        }
      }
    }

  }

  private def querySubClasses(expression: OWLClassExpression, direct: Boolean): Set[OWLClass] =
    reasoner.getSubClasses(expression, direct).getFlattened.asScala.toSet.filterNot(_.isOWLNothing)

  private def queryEquivalentClasses(expression: OWLClassExpression): Set[OWLClass] =
    reasoner.getEquivalentClasses(expression).getEntities.asScala.toSet

  private def querySuperClasses(expression: OWLClassExpression, direct: Boolean): Set[OWLClass] =
    reasoner.getSuperClasses(expression, direct).getFlattened.asScala.toSet.filterNot(_.isOWLThing)

  private def queryIndividuals(expression: OWLClassExpression, direct: Boolean): Set[OWLNamedIndividual] =
    reasoner.getInstances(expression, direct).getFlattened.asScala.toSet

  private def matchTriple(triple: TriplePath, prefixes: Map[String, String]): Option[OwletResult] =
    for {
      (expression, queryFunction) <- (triple.getSubject, triple.getPredicate, triple.getObject) match {
        case (_: Node_Variable | Node.ANY, Owlet.SubClassOf, expression: Node_Literal) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI)       =>
          Option(expression, querySubClasses(_, false))
        case (_: Node_Variable | Node.ANY, Owlet.DirectSubClassOf, expression: Node_Literal) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI) =>
          Option(expression, querySubClasses(_, true))
        case (expression: Node_Literal, Owlet.SubClassOf, _: Node_Variable | Node.ANY) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI)       =>
          Option(expression, querySuperClasses(_, false))
        case (expression: Node_Literal, Owlet.DirectSubClassOf, _: Node_Variable | Node.ANY) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI) =>
          Option(expression, querySuperClasses(_, true))
        case (_: Node_Variable | Node.ANY, Owlet.EquivalentClass, expression: Node_Literal) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI)  =>
          Option(expression, queryEquivalentClasses _)
        case (expression: Node_Literal, Owlet.EquivalentClass, _: Node_Variable | Node.ANY) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI)  =>
          Option(expression, queryEquivalentClasses _)
        case (_: Node_Variable | Node.ANY, Owlet.Type, expression: Node_Literal) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI)             =>
          Option(expression, queryIndividuals(_, false))
        case (_: Node_Variable | Node.ANY, Owlet.DirectType, expression: Node_Literal) if Owlet.SYNTAXES(expression.getLiteralDatatypeURI)       =>
          Option(expression, queryIndividuals(_, true))
        case _                                                                                                                                   => None
      }
      classExpression <- Owlet.parseExpression(expression, prefixes)
    } yield OwletResult(triple.asTriple, queryFunction(classExpression))

}

object Owlet {

  private val SubClassOf: Node = RDFS.Nodes.subClassOf
  private val DirectSubClassOf: Node = NodeFactory.createURI(SESAME.DIRECTSUBCLASSOF.stringValue())
  private val EquivalentClass: Node = OWL2.equivalentClass.asNode
  private val Type: Node = RDF.Nodes.`type`
  private val DirectType: Node = NodeFactory.createURI(SESAME.DIRECTTYPE.stringValue())

  val OWLET_NS: String = "http://purl.org/phenoscape/owlet/syntax#"
  val MANCHESTER: String = s"${OWLET_NS}omn"
  val OWLXML: String = s"${OWLET_NS}owx"
  val FUNCTIONAL: String = s"${OWLET_NS}ofn"
  val SYNTAXES: Set[String] = Set(MANCHESTER, OWLXML, FUNCTIONAL)

  def runQueryAndMakeFilter(queryFunction: OWLClassExpression => Set[_ <: OWLEntity], classExpression: Node_Literal, prefixes: Map[String, String], variable: Node_Variable): Option[ElementFilter] =
    parseExpression(classExpression, prefixes).map { expression =>
      makeFilter(variable, queryFunction(expression))
    }

  private def parseExpression(literal: Node_Literal, prefixes: Map[String, String]): Option[OWLClassExpression] = {
    val expression = literal.getLiteralLexicalForm
    literal.getLiteralDatatypeURI match {
      case MANCHESTER => ManchesterSyntaxClassExpressionParser.parse(expression, prefixes).toOption
      case _          => None
    }
  }


  def makeFilter(variable: Node_Variable, classes: Iterable[OWLEntity]): ElementFilter = {
    val nodes = classes.map(term => new NodeValueNode(NodeFactory.createURI(term.getIRI.toString)))
    val oneOf = new E_OneOf(new ExprVar(variable), new ExprList((nodes.toList: List[Expr]).asJava))
    new ElementFilter(oneOf)
  }

  def makeValuesBlock(variable: Node_Variable, classes: Iterable[OWLEntity]): ElementData = {
    val nodes = classes.map(term => NodeFactory.createURI(term.getIRI.toString))
    val data = new ElementData()
    val bindingVar = Var.alloc(variable)
    data.add(bindingVar)
    nodes.foreach { node =>
      data.add(BindingFactory.binding(bindingVar, node))
    }
    data
  }

}