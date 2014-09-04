package org.phenoscape.owlet

import scala.collection.JavaConverters.asScalaSetConverter
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLEntity
import org.semanticweb.owlapi.model.OWLProperty
import org.semanticweb.owlapi.reasoner.OWLReasoner
import com.hp.hpl.jena.graph.Node
import com.hp.hpl.jena.graph.NodeFactory
import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.sparql.core.TriplePath
import com.hp.hpl.jena.sparql.core.Var
import com.hp.hpl.jena.sparql.expr.E_Str
import com.hp.hpl.jena.sparql.expr.ExprVar
import com.hp.hpl.jena.sparql.path.P_Link
import com.hp.hpl.jena.sparql.path.P_Seq
import com.hp.hpl.jena.sparql.path.Path
import com.hp.hpl.jena.sparql.syntax.Element
import com.hp.hpl.jena.sparql.syntax.ElementFilter
import com.hp.hpl.jena.sparql.syntax.ElementGroup
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock
import com.hp.hpl.jena.sparql.path.P_ZeroOrMore1
import com.hp.hpl.jena.sparql.syntax.ElementService

object SPARQLComposer {

  def select(resultVars: Var*) = {
    val query = QueryFactory.make()
    query.setQuerySelectType()
    resultVars foreach { query.addResultVar(_) }
    query
  }

  def select_distinct(resultVars: Var*) = {
    val query = select(resultVars: _*)
    query.setDistinct(true)
    query
  }

  def bgp(triples: TriplePath*): ElementPathBlock = {
    val block = new ElementPathBlock()
    triples.foreach(block.addTriplePath)
    block
  }

  def service(uri: String, elements: Element*): ElementService = {
    val body = new ElementGroup()
    elements.foreach(body.addElement)
    new ElementService(uri, body)
  }

  def subClassOf(variable: Symbol, expression: OWLClassExpression)(implicit reasoner: OWLReasoner): ElementFilter = {
    val subclasses = reasoner.getSubClasses(expression, false).getFlattened.asScala
    val filterClasses = if (!expression.isAnonymous)
      subclasses + expression.asOWLClass
    else
      subclasses
    Owlet.makeFilter(variable, filterClasses)
  }

  def str(variable: Var): E_Str = new E_Str(new ExprVar(variable))

  implicit def symbolToVar(value: Symbol): Var = Var.alloc(value.name)

  implicit def iriToNode(iri: IRI): Node = NodeFactory.createURI(iri.toString)

  implicit def iriToPath(iri: IRI): P_Link = new P_Link(iriToNode(iri))

  implicit def owlEntityToNode(entity: OWLEntity): Node = iriToNode(entity.getIRI)

  implicit def owlEntityToPath(entity: OWLEntity): P_Link = new P_Link(owlEntityToNode(entity))

  def t(s: Node, p: Path, o: Node): TriplePath = new TriplePath(s, p, o)

  implicit class ComposerQuery(val self: Query) extends AnyVal {

    def from(uri: String): Query = {
      self.addGraphURI(uri)
      self
    }

    def where(elements: Element*): Query = {
      val body = new ElementGroup()
      elements foreach { body.addElement(_) }
      self.setQueryPattern(body)
      self
    }

  }

  implicit class ComposerProperty(val self: OWLProperty[_, _]) extends AnyVal {

    def /(rightSide: Path): P_Seq = {
      new P_Seq(new P_Link(owlEntityToNode(self)), rightSide)
    }

    def * : P_ZeroOrMore1 = {
      new P_ZeroOrMore1(self)
    }

  }

  implicit class ComposerPath(val self: Path) extends AnyVal {

    def /(rightSide: Path): P_Seq = {
      new P_Seq(self, rightSide)
    }

  }

  //  implicit class ComposerExprFunction(val self: ExprFunction) extends AnyVal {
  //    
  //    def as(variable: Var): 
  //    
  //  }

}