package org.phenoscape.owlet;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestManchesterParserJava {
	
	/**
	 * Demonstrate that we can use the parser from Java.
	 */
	@Test
	public void testExpressions() {
		final Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("ex", "http://example.org/");
		ManchesterSyntaxClassExpressionParser.parse("ex:head and not (ex:part_of some ex:eye)", prefixes);
	}

}
