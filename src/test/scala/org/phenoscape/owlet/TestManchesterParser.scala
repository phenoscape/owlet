package org.phenoscape.owlet

import org.junit.Assert
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import scalaz.Validation

class TestManchesterParser {

  val factory = OWLManager.getOWLDataFactory

  @Test
  def testExpressions(): Unit = {
    val head = factory.getOWLClass(IRI.create("http://example.org/head"))
    val cell = factory.getOWLClass(IRI.create("http://example.org/cell"))
    val nucleus = factory.getOWLClass(IRI.create("http://example.org/nucleus"))
    val muscle = factory.getOWLClass(IRI.create("http://example.org/muscle"))
    val eye = factory.getOWLClass(IRI.create("http://example.org/eye"))
    val part_of = factory.getOWLObjectProperty(IRI.create("http://example.org/part_of"))
    val has_part = factory.getOWLObjectProperty(IRI.create("http://example.org/has_part"))

    val parsed1 = ManchesterSyntaxClassExpressionParser.parse("ex:head", Map("ex" -> "http://example.org/")).toOption.get
    Assert.assertTrue(parsed1.isInstanceOf[OWLClass])
    Assert.assertEquals(head, parsed1)

    val parsed2 = ManchesterSyntaxClassExpressionParser.parse("head").toOption
    Assert.assertEquals(None, parsed2)

    val parsed3 = ManchesterSyntaxClassExpressionParser.parse("head:", Map("head" -> "http://example.org/head")).toOption.get
    Assert.assertEquals(head, parsed3)

    val parsed4 = ManchesterSyntaxClassExpressionParser.parse(":head", Map("" -> "http://example.org/")).toOption.get
    Assert.assertEquals(head, parsed4)

    val parsed5 = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/head> or <http://example.org/muscle>").toOption.get
    Assert.assertEquals(factory.getOWLObjectUnionOf(head, muscle), parsed5)

    val parsed6 = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/muscle> and <http://example.org/part_of> some <http://example.org/head>").toOption.get
    Assert.assertEquals(factory.getOWLObjectIntersectionOf(muscle, factory.getOWLObjectSomeValuesFrom(part_of, head)), parsed6)

    val parsed6a = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/muscle> that <http://example.org/part_of> some <http://example.org/head>").toOption.get

    Assert.assertEquals(factory.getOWLObjectIntersectionOf(muscle, factory.getOWLObjectSomeValuesFrom(part_of, head)), parsed6a)

    val parsed6b = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/muscle> that not (<http://example.org/part_of> some <http://example.org/head>)").toOption.get
    Assert.assertEquals(factory.getOWLObjectIntersectionOf(muscle, factory.getOWLObjectComplementOf(factory.getOWLObjectSomeValuesFrom(part_of, head))), parsed6b)

    val parsed6c = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/muscle> that not (<http://example.org/part_of> some <http://example.org/head>) and <http://example.org/part_of> some <http://example.org/eye>").toOption.get
    Assert.assertEquals(factory.getOWLObjectIntersectionOf(muscle, factory.getOWLObjectComplementOf(factory.getOWLObjectSomeValuesFrom(part_of, head)), factory.getOWLObjectSomeValuesFrom(part_of, eye)), parsed6c)

    val parsed7 = ManchesterSyntaxClassExpressionParser.parse("not <http://example.org/muscle> and <http://example.org/part_of> some <http://example.org/head>").toOption.get
    Assert.assertEquals(factory.getOWLObjectIntersectionOf(factory.getOWLObjectComplementOf(muscle), factory.getOWLObjectSomeValuesFrom(part_of, head)), parsed7)

    val parsed8 = ManchesterSyntaxClassExpressionParser.parse("ex:head and not (ex:part_of some ex:eye)", Map("ex" -> "http://example.org/")).toOption.get
    Assert.assertEquals(factory.getOWLObjectIntersectionOf(head, factory.getOWLObjectComplementOf(factory.getOWLObjectSomeValuesFrom(part_of, eye))), parsed8)

    val parsed9 = ManchesterSyntaxClassExpressionParser.parse("not(ex:head)", Map("ex" -> "http://example.org/")).toOption.get
    Assert.assertEquals(factory.getOWLObjectComplementOf(head), parsed9)

    val parsed10 = ManchesterSyntaxClassExpressionParser.parse("(ex:head or ex:cell) and not(ex:nucleus) and ex:part_of some (ex:eye or ex:muscle)", Map("ex" -> "http://example.org/")).toOption.get
    val built10 = factory.getOWLObjectIntersectionOf(
      factory.getOWLObjectUnionOf(head, cell),
      factory.getOWLObjectComplementOf(nucleus),
      factory.getOWLObjectSomeValuesFrom(part_of, factory.getOWLObjectUnionOf(eye, muscle)))
    Assert.assertEquals(built10, parsed10)

    val parsed11 = ManchesterSyntaxClassExpressionParser.parse("ex:cell and ex:has_part max 1 ex:nucleus or ex:eye", Map("ex" -> "http://example.org/")).toOption.get
    val built11 = factory.getOWLObjectUnionOf(
      factory.getOWLObjectIntersectionOf(cell, factory.getOWLObjectMaxCardinality(1, has_part, nucleus)),
      eye)
    Assert.assertEquals(built11, parsed11)

    val parsed12 = ManchesterSyntaxClassExpressionParser.parse("", Map("ex" -> "http://example.org/"))
    Assert.assertTrue(parsed12.isFailure)
  }

  @Test
  def testUUIDExpression(): Unit = {
    // the prefix regex performed worse and worse as the local name got longer
    val prefixes = Map("" -> "http://purl.obolibrary.org/obo/")
    val parsed = ManchesterSyntaxClassExpressionParser.parse(":UBERONTEMP_18a5dd1b-1213-471f-9a0b-06190b1ecf2c", prefixes)
    Assert.assertTrue(parsed.toOption.get.isInstanceOf[OWLClass]) // problem is that parsing never returns
  }

  @Test
  def testPrefixError(): Unit = {
    val prefixes = Map("" -> "http://purl.obolibrary.org/obo/")
    val expression = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> some <http://purl.obolibrary.org/obo/UBERON_0000981>"
    val parsed = ManchesterSyntaxClassExpressionParser.parse(expression, prefixes)
    Assert.assertTrue(parsed.toOption.get.isInstanceOf[OWLClassExpression])
  }

  @Test
  def testFailOnMissingPrefix(): Unit = {
    val expression = "ex:head and not (ex:part_of some ex:eye)"
    val parsed = ManchesterSyntaxClassExpressionParser.parse(expression).toOption
    Assert.assertEquals(None, parsed)
    val parsedWithPrefixes = ManchesterSyntaxClassExpressionParser.parse(expression, Map("ex" -> "http://example.org/"))
    Assert.assertTrue(parsedWithPrefixes.toOption.get.isInstanceOf[OWLClassExpression])
  }

  @Test
  def testIRIParsing(): Unit = {
    val parsed1 = ManchesterSyntaxClassExpressionParser.parseIRI("ex:head", Map("ex" -> "http://example.org/")).toOption.get
    Assert.assertEquals(IRI.create("http://example.org/head"), parsed1)
    val parsed2 = ManchesterSyntaxClassExpressionParser.parseIRI("<http://example.org/head>").toOption.get
    Assert.assertEquals(IRI.create("http://example.org/head"), parsed2)
  }

}