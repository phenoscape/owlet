package org.phenoscape.owlet

import org.semanticweb.owlapi.reasoner.OWLReasoner
import com.hp.hpl.jena.graph.Triple
import com.hp.hpl.jena.graph.TripleMatch
import com.hp.hpl.jena.graph.impl.GraphBase
import com.hp.hpl.jena.util.iterator.ExtendedIterator
import com.hp.hpl.jena.sparql.core.TriplePath
import com.hp.hpl.jena.util.iterator.NiceIterator
import com.hp.hpl.jena.util.iterator.WrappedIterator
import scala.collection.JavaConversions._

class OwletGraph(reasoner: OWLReasoner, prefixes: Map[String, String]) extends GraphBase {

  override def graphBaseFind(pattern: TripleMatch): ExtendedIterator[Triple] = {
    println("Should be called")
    println(pattern.asTriple())
    val owlet = new QueryExpander(this.reasoner)
    //FIXME owlet will not process the triple because variable is ANY instead of NodeVariable
    val results = owlet.matchTriple(new TriplePath(pattern.asTriple), prefixes).map(_.toTriples).getOrElse(Set())
    println(results)
    WrappedIterator.create(results.iterator)
  }

}