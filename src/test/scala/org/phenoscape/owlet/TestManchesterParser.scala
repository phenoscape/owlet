package org.phenoscape.owlet

import org.junit.Assert
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.{IRI, OWLClass}

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
					eye
					)
			Assert.assertEquals(built11, parsed11)
	}

}