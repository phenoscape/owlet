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
import com.hp.hpl.jena.datatypes.TypeMapper
import com.hp.hpl.jena.datatypes.RDFDatatype
import com.hp.hpl.jena.sparql.syntax.ElementOptional
import com.hp.hpl.jena.query.SortCondition
import com.hp.hpl.jena.sparql.path.P_Alt
import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.sparql.core.BasicPattern
import scala.collection.JavaConverters._
import com.hp.hpl.jena.graph.Triple
import com.hp.hpl.jena.sparql.syntax.Template
import com.hp.hpl.jena.sparql.modify.request.UpdateWithUsing
import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteInsert

object SPARQLComposer {

  def construct(triples: BasicTriple*): Query = {
    val query = QueryFactory.make()
    query.setQueryConstructType()
    val bgp = BasicPattern.wrap(triples.map(triple => new Triple(triple.s, triple.p, triple.o)).asJava)
    query.setConstructTemplate(new Template(bgp))
    query
  }

  def select(resultVars: Var*): Query = {
    val query = QueryFactory.make()
    query.setQuerySelectType()
    resultVars foreach { query.addResultVar(_) }
    query
  }

  def select_distinct(resultVars: Var*): Query = {
    val query = select(resultVars: _*)
    query.setDistinct(true)
    query
  }

  //  def WITH(uri: String): UpdateDeleteInsert = {
  //    val update = new UpdateDeleteInsert()
  //    update.setWithIRI(NodeFactory.createURI(uri))
  //    update
  //  }

  //  def INSERT: UpdateDeleteInsert = {
  //    val update = new UpdateDeleteInsert()
  //   update.
  //    update
  //  }

  def bgp(triples: TripleOrPath*): ElementPathBlock = {
    val block = new ElementPathBlock()
    triples.foreach {
      case triple: BasicTriple => block.addTriple(new Triple(triple.s, triple.p, triple.o))
      case path: PathTriple => block.addTriplePath(new TriplePath(path.s, path.p, path.o))
    }
    block
  }

  def optional(elements: Element*): ElementOptional = {
    val body = new ElementGroup()
    elements.foreach(body.addElement)
    new ElementOptional(body)
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

  def asc(variable: Var): SortCondition = new SortCondition(variable, Query.ORDER_ASCENDING)

  def desc(variable: Var): SortCondition = new SortCondition(variable, Query.ORDER_DESCENDING)

  implicit def symbolToVar(value: Symbol): Var = Var.alloc(value.name)

  implicit def symbolToSortCondition(value: Symbol): SortCondition = new SortCondition(symbolToVar(value), Query.ORDER_DEFAULT)

  implicit def iriToNode(iri: IRI): Node = NodeFactory.createURI(iri.toString)

  //implicit def iriToPath(iri: IRI): P_Link = new P_Link(iriToNode(iri))

  implicit def owlEntityToNode(entity: OWLEntity): Node = iriToNode(entity.getIRI)

  //implicit def owlEntityToPath(entity: OWLEntity): P_Link = new P_Link(owlEntityToNode(entity))

  def t(s: Node, p: Path, o: Node): PathTriple = PathTriple(s, p, o)

  def t(s: Node, p: Node, o: Node): BasicTriple = BasicTriple(s, p, o)

  sealed trait TripleOrPath

  case class BasicTriple(s: Node, p: Node, o: Node) extends TripleOrPath

  case class PathTriple(s: Node, p: Path, o: Node) extends TripleOrPath

  implicit def triplePathToTriple(tp: TriplePath): Triple = tp.asTriple

  implicit class StringToLiteral(val self: String) extends AnyVal {

    def ^^(datatypeURI: String): Node = {
      ^^(TypeMapper.getInstance.getSafeTypeByName(datatypeURI))
    }

    def ^^(datatype: RDFDatatype): Node = {
      NodeFactory.createLiteral(self, datatype)
    }

  }

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

    def order_by(sortConditions: SortCondition*): Query = {
      sortConditions.foreach(self.addOrderBy(_))
      self
    }

    def limit(count: Int): Query = {
      self.setLimit(count)
      self
    }

    def into[T](func: QuerySolution => T): Query = {
      ???
    }

  }

  implicit class ComposerProperty(val self: OWLProperty[_, _]) extends AnyVal {

    def /(rightSide: Path): P_Seq = new P_Seq(new P_Link(owlEntityToNode(self)), rightSide)

    def /(rightSide: Node): P_Seq = new P_Seq(new P_Link(owlEntityToNode(self)), new P_Link(rightSide))

    def |(rightSide: Path): P_Alt = new P_Alt(new P_Link(owlEntityToNode(self)), rightSide)
    
    def |(rightSide: Node): P_Alt = new P_Alt(new P_Link(owlEntityToNode(self)), new P_Link(rightSide))

    def * : P_ZeroOrMore1 = new P_ZeroOrMore1(new P_Link(owlEntityToNode(self)))

  }

  implicit class ComposerPath(val self: Path) extends AnyVal {

    def /(rightSide: Path): P_Seq = new P_Seq(self, rightSide)

    def /(rightSide: Node): P_Seq = new P_Seq(self, new P_Link(rightSide))

  }

  //  implicit class ComposerExprFunction(val self: ExprFunction) extends AnyVal {
  //    
  //    def as(variable: Var): 
  //    
  //  }

}