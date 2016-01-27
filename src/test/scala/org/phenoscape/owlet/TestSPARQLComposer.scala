package org.phenoscape.owlet

import org.junit.Test
import org.phenoscape.owlet.SPARQLComposer._
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.model.IRI
import org.junit.Assert
import org.phenoscape.owlet.OwletManchesterSyntaxDataType._
import com.hp.hpl.jena.sparql.expr.ExprVar

class TestSPARQLComposer {

  val omn = Owlet.MANCHESTER
  val factory = OWLManager.getOWLDataFactory

  @Test
  def testZeroOrMorePropertyPath(): Unit = {
    val head = factory.getOWLClass(IRI.create("http://example.org/head"))
    val muscle = factory.getOWLClass(IRI.create("http://example.org/muscle"))
    val part_of = factory.getOWLObjectProperty(IRI.create("http://example.org/part_of"))
    val expression = factory.getOWLObjectIntersectionOf(muscle, factory.getOWLObjectSomeValuesFrom(part_of, head))
    val rdfsSubClassOf = factory.getOWLObjectProperty(OWLRDFVocabulary.RDFS_SUBCLASS_OF.getIRI)
    val rdfType = factory.getOWLObjectProperty(OWLRDFVocabulary.RDF_TYPE.getIRI)
    val query = select_distinct('phenotype) from "http://kb.phenoscape.org/" where (
      bgp(
        t('eq, rdfsSubClassOf*, 'absence),
        t('phenotype, rdfType, 'eq)),
        service("http://owlery.phenoscape.org/sparql",
          bgp(
            t('eq, rdfsSubClassOf, "part_of some blah" ^^ omn),
            t('eq, rdfsSubClassOf, expression.asOMN)))) order_by 'phenotype
    println(query)
  }

  @Test
  def constructQuery(): Unit = {
    val head = factory.getOWLClass(IRI.create("http://example.org/head"))
    val muscle = factory.getOWLClass(IRI.create("http://example.org/muscle"))
    val part_of = factory.getOWLObjectProperty(IRI.create("http://example.org/part_of"))
    val expression = factory.getOWLObjectIntersectionOf(muscle, factory.getOWLObjectSomeValuesFrom(part_of, head))
    val rdfsSubClassOf = factory.getOWLObjectProperty(OWLRDFVocabulary.RDFS_SUBCLASS_OF.getIRI)
    val rdfType = factory.getOWLObjectProperty(OWLRDFVocabulary.RDF_TYPE.getIRI)
    val query = construct(t('phenotype, rdfType, 'eq)) from "http://kb.phenoscape.org/" where (
      bgp(
        t('eq, rdfsSubClassOf*, 'absence),
        t('phenotype, rdfType, 'eq),
        t('foo, 'pred, 'bar)),
        service("http://owlery.phenoscape.org/sparql",
          bgp(
            t('eq, rdfsSubClassOf, "part_of some blah" ^^ omn),
            t('blah, rdfsSubClassOf / rdfType, 'blah),
            t('blah, rdfsSubClassOf / (rdfType *), 'blah),
            t('blah, rdfsSubClassOf | (rdfType*) / part_of, 'blah),
            t('blah, rdfsSubClassOf ?, 'blah),
            t('blah, rdfsSubClassOf +, 'blah),
            t('foo, 'pred, 'bar),
            t('eq, rdfsSubClassOf, expression.asOMN)),
          filter('eq in (owlEntityToNode(rdfType) :: owlEntityToNode(rdfsSubClassOf) :: Nil)))) order_by 'phenotype
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
