package org.nescent.owl.filter

import org.junit.Assert
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLClass

class TestManchesterParser {
  
	val factory = OWLManager.getOWLDataFactory();

	@Test
	def testExpressions(): Unit = {
			val head = IRI.create("http://purl.obolibrary.org/obo/uberon/head");
			
			val parsed1 = ManchesterSyntaxClassExpressionParser.parse("uberon:head", Map("uberon" -> "http://purl.obolibrary.org/obo/uberon/")).get;
			Assert.assertTrue(parsed1.isInstanceOf[OWLClass]);
			Assert.assertEquals(factory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/uberon/head")), parsed1);
			
			val parsed2 = ManchesterSyntaxClassExpressionParser.parse("head");
			Assert.assertEquals(None, parsed2);
			
			val parsed3 = ManchesterSyntaxClassExpressionParser.parse("head:", Map("head" -> "http://purl.obolibrary.org/obo/uberon/head")).get;
			Assert.assertEquals(factory.getOWLClass(head), parsed3);
			
			val parsed4 = ManchesterSyntaxClassExpressionParser.parse(":head", Map("" -> "http://purl.obolibrary.org/obo/uberon/")).get;
			Assert.assertEquals(factory.getOWLClass(head), parsed4);
			
			val parsed5 = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/head> or <http://example.org/muscle>").get;
			Assert.assertEquals(factory.getOWLObjectUnionOf(factory.getOWLClass(IRI.create("http://example.org/head")), factory.getOWLClass(IRI.create("http://example.org/muscle"))), parsed5);
			
			val parsed6 = ManchesterSyntaxClassExpressionParser.parse("<http://example.org/muscle> and <http://example.org/part_of> some <http://example.org/head>").get;
			
			val parsed7 = ManchesterSyntaxClassExpressionParser.parse("not <http://example.org/muscle> and <http://example.org/part_of> some <http://example.org/head>").get;
			
			val parsed8 = ManchesterSyntaxClassExpressionParser.parse("u:head and not (u:part_of some u:body)", Map("u" -> "http://uberon.org/")).get;
			
			val parsed9 = ManchesterSyntaxClassExpressionParser.parse("not(u:head)", Map("u" -> "http://uberon.org/")).get;
			
			val parsed10 = ManchesterSyntaxClassExpressionParser.parse("(u:brain or u:bone) and not(u:head) and u:part_of some (u:eye or u:tail)", Map("u" -> "http://uberon.org/")).get;
	}

}