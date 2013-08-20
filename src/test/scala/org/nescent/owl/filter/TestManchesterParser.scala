package org.nescent.owl.filter

import org.junit.Test

class TestManchesterParser {

	@Test
	def testExpressions(): Unit = {
			//FIXME create proper tests
			println(ManchesterSyntaxClassExpressionParser.parse("uberon:head", Map("uberon" -> "http://purl.obolibrary.org/obo/uberon/")));
			println(ManchesterSyntaxClassExpressionParser.parse("head"));
			println(ManchesterSyntaxClassExpressionParser.parse("head:", Map("head" -> "http://purl.obolibrary.org/obo/uberon/head")));
			println(ManchesterSyntaxClassExpressionParser.parse(":head", Map("" -> "http://example.org/base/")));
			println(ManchesterSyntaxClassExpressionParser.parse("<http://example.org/head> or <http://example.org/muscle>"));
			println(ManchesterSyntaxClassExpressionParser.parse("<http://example.org/muscle> and <http://example.org/part_of> some <http://example.org/head>"));
			println(ManchesterSyntaxClassExpressionParser.parse("not <http://example.org/muscle> and <http://example.org/part_of> some <http://example.org/head>"));
			println(ManchesterSyntaxClassExpressionParser.parse("u:head and not (u:part_of some u:body)", Map("u" -> "http://uberon.org/")));
			println(ManchesterSyntaxClassExpressionParser.parse("not(u:head)", Map("u" -> "http://uberon.org/")));
			println(ManchesterSyntaxClassExpressionParser.parse("(u:brain or u:bone) and not(u:head) and u:part_of some (u:eye or u:tail)", Map("u" -> "http://uberon.org/")));
	}

}