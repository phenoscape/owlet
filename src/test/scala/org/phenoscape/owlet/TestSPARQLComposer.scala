package org.phenoscape.owlet

import org.junit.Test
import org.phenoscape.owlet.SPARQLComposer._
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary

class TestSPARQLComposer {

  @Test
  def testZeroOrMorePropertyPath(): Unit = {
    val factory = OWLManager.getOWLDataFactory
    val rdfsSubClassOf = factory.getOWLObjectProperty(OWLRDFVocabulary.RDFS_SUBCLASS_OF.getIRI)
    val rdfType = factory.getOWLObjectProperty(OWLRDFVocabulary.RDF_TYPE.getIRI)
    val query = select_distinct('phenotype) from "http://kb.phenoscape.org/" where (
      bgp(
        t('eq, rdfsSubClassOf*, 'absence),
        t('phenotype, rdfType, 'eq)))
    println(query)
  }

}