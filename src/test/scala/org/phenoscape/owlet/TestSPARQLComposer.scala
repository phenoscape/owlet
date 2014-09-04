package org.phenoscape.owlet

import org.junit.Test
import org.phenoscape.owlet.SPARQLComposer._
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.model.IRI
import org.phenoscape.owlet.OwletManchesterSyntaxDataType._
import org.junit.Assert

class TestSPARQLComposer {

  val omn = OwletManchesterSyntaxDataType
  val factory = OWLManager.getOWLDataFactory

  @Test
  def testZeroOrMorePropertyPath(): Unit = {

    val rdfsSubClassOf = factory.getOWLObjectProperty(OWLRDFVocabulary.RDFS_SUBCLASS_OF.getIRI)
    val rdfType = factory.getOWLObjectProperty(OWLRDFVocabulary.RDF_TYPE.getIRI)
    val query = select_distinct('phenotype) from "http://kb.phenoscape.org/" where (
      bgp(
        t('eq, rdfsSubClassOf*, 'absence),
        t('phenotype, rdfType, 'eq)),
        service("http://owlery.phenoscape.org/sparql",
          bgp(
            t('eq, rdfsSubClassOf, "part_of some blah" ^^ omn))))
    println(query)
  }

  @Test
  def testManchesterSyntaxRenderer(): Unit = {
    val head = factory.getOWLClass(IRI.create("http://example.org/head"))
    val muscle = factory.getOWLClass(IRI.create("http://example.org/muscle"))
    val part_of = factory.getOWLObjectProperty(IRI.create("http://example.org/part_of"))
    val expression = factory.getOWLObjectIntersectionOf(muscle, factory.getOWLObjectSomeValuesFrom(part_of, head))
    Assert.assertEquals("\"<http://example.org/muscle> and (<http://example.org/part_of> some <http://example.org/head>)\"^^http://purl.org/phenoscape/owlet/syntax#omn",
      expression.asOMN.toString)
  }

}