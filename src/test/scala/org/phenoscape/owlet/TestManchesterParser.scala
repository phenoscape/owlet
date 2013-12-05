package org.phenoscape.owlet

import org.junit.Assert
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{ IRI, OWLClass }

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

    val parsed1 = ManchesterSyntaxClassExpressionParser.parse("ex:head", Map("ex" -> "http://example.org/")).get
    Assert.assertTrue(parsed1.isInstanceOf[OWLClass])
    Assert.assertEquals(head, parsed1)

    val parsed2 = ManchesterSyntaxClassExpressionParser.parse("head")
    Assert.assertEquals(None, parsed2)

    val parsed3 = ManchesterSyntaxClassExpressionParser.parse("head:", Map("head" -> "http://example.org/head")).get
    Assert.assertEquals(head, parsed3)

    val parsed4 = ManchesterSyntaxClassExpressionParser.parse(":head", Map("" -> "http://example.org/")).get
    Assert.assertEquals(head, parsed4)

    val parsed5 = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/head> or <http://example.org/muscle>").get
    Assert.assertEquals(factory.getOWLObjectUnionOf(head, muscle), parsed5)

    val parsed6 = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/muscle> and <http://example.org/part_of> some <http://example.org/head>").get
    Assert.assertEquals(factory.getOWLObjectIntersectionOf(muscle, factory.getOWLObjectSomeValuesFrom(part_of, head)), parsed6)

    val parsed7 = ManchesterSyntaxClassExpressionParser.parse("not <http://example.org/muscle> and <http://example.org/part_of> some <http://example.org/head>").get
    Assert.assertEquals(factory.getOWLObjectIntersectionOf(factory.getOWLObjectComplementOf(muscle), factory.getOWLObjectSomeValuesFrom(part_of, head)), parsed7)

    val parsed8 = ManchesterSyntaxClassExpressionParser.parse("ex:head and not (ex:part_of some ex:eye)", Map("ex" -> "http://example.org/")).get
    Assert.assertEquals(factory.getOWLObjectIntersectionOf(head, factory.getOWLObjectComplementOf(factory.getOWLObjectSomeValuesFrom(part_of, eye))), parsed8)

    val parsed9 = ManchesterSyntaxClassExpressionParser.parse("not(ex:head)", Map("ex" -> "http://example.org/")).get
    Assert.assertEquals(factory.getOWLObjectComplementOf(head), parsed9)

    val parsed10 = ManchesterSyntaxClassExpressionParser.parse("(ex:head or ex:cell) and not(ex:nucleus) and ex:part_of some (ex:eye or ex:muscle)", Map("ex" -> "http://example.org/")).get
    val built10 = factory.getOWLObjectIntersectionOf(
      factory.getOWLObjectUnionOf(head, cell),
      factory.getOWLObjectComplementOf(nucleus),
      factory.getOWLObjectSomeValuesFrom(part_of, factory.getOWLObjectUnionOf(eye, muscle)))
    Assert.assertEquals(built10, parsed10)

    val parsed11 = ManchesterSyntaxClassExpressionParser.parse("ex:cell and ex:has_part max 1 ex:nucleus or ex:eye", Map("ex" -> "http://example.org/")).get
    val built11 = factory.getOWLObjectUnionOf(
      factory.getOWLObjectIntersectionOf(cell, factory.getOWLObjectMaxCardinality(1, has_part, nucleus)),
      eye)
    Assert.assertEquals(built11, parsed11)
  }

  @Test
  def testUUIDExpression(): Unit = {
    val PN_CHARS_BASE = "[A-Z]|[a-z]|[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]|[\u10000-\uEFFFF]"
    val PN_CHARS_U = """(%s)|_""".format(PN_CHARS_BASE)
    val PN_CHARS = """(%s)|\-|[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040]""".format(PN_CHARS_U)
    val PN_PREFIX = """(%s)(((%s)|\.)*(%s))?""".format(PN_CHARS_BASE, PN_CHARS, PN_CHARS)
    val PN_LOCAL = """((%s)|[0-9])(((%s)|\.)*(%s))?""".format(PN_CHARS_U, PN_CHARS, PN_CHARS)
    val PNAME_NS = """(%s)?:""".format(PN_PREFIX)
    val PNAME_LN = "%s(%s)*".format(PNAME_NS, PN_LOCAL)
    println(PNAME_LN)
    val regex = PNAME_LN.r
    //println(regex.findFirstMatchIn(":UBERONTEMP_18a5dd1b-1213-471f-9a0b-06190b1ecf2c"))
    println(regex.findFirstMatchIn(":UBERONTEMP_18a5dd1b-"))

//    val prefixes = Map("" -> "http://purl.obolibrary.org/obo/")
//    val parsed = ManchesterSyntaxClassExpressionParser.parse(":UBERONTEMP_18a5dd1b-1213-471f-9a0b-06190b1ecf2c", prefixes)
//    Assert.assertTrue(parsed.isInstanceOf[OWLClass]) // problem is that parsing never returns
  }

}