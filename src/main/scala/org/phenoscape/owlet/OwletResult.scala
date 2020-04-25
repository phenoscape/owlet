package org.phenoscape.owlet

import org.apache.jena.graph.{Node, NodeFactory, Node_Variable, Triple}
import org.apache.jena.sparql.syntax.{ElementData, ElementFilter}
import org.semanticweb.owlapi.model.OWLEntity

case class OwletResult(triple: Triple, terms: Set[_ <: OWLEntity]) {

  /**
   * @return an optional ElementFilter: if the starting triple contained Node.ANY rather
   *         than a variable, a filter cannot be created
   */
  def toFilter: Option[ElementFilter] = findVariable.map(Owlet.makeFilter(_, terms))

  /**
   * @return an optional ElementData (values block): if the starting triple contained Node.ANY rather
   *         than a variable, a values block cannot be created
   */
  def toValues: Option[ElementData] = findVariable.map(Owlet.makeValuesBlock(_, terms))

  private def findVariable: Option[Node_Variable] =
    (triple.getSubject, triple.getPredicate, triple.getObject) match {
      case (variableNode: Node_Variable, _, _) => Option(variableNode)
      case (_, _, variableNode: Node_Variable) => Option(variableNode)
      case _                                   => None
    }

  def toTriples: Set[Triple] = {
    val nodeToTriple = (triple.getSubject, triple.getPredicate, triple.getObject) match {
      case (_: Node_Variable | Node.ANY, predicateNode, objectNode)  =>
        in: Node => Triple.create(in, predicateNode, objectNode)
      case (subjectNode, predicateNode, _: Node_Variable | Node.ANY) =>
        in: Node => Triple.create(subjectNode, predicateNode, in)
    }
    terms.map(term => nodeToTriple(NodeFactory.createURI(term.getIRI.toString)))
  }

}
