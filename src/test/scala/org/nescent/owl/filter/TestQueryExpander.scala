package org.nescent.owl.filter

import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.jena.riot.out.EscapeStr
import org.junit.Test
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.reasoner.InferenceType
import org.junit.BeforeClass
import org.semanticweb.owlapi.reasoner.OWLReasoner
import org.junit.AfterClass

object TestQueryExpander {

	; var reasoner: OWLReasoner = null;

	@BeforeClass
	def setupReasoner(): Unit = {
			val manager = OWLManager.createOWLOntologyManager();
			val uberonStream = getClass().getClassLoader().getResourceAsStream("uberon.owl");
			val uberon = manager.loadOntologyFromOntologyDocument(uberonStream);
			reasoner = new ElkReasonerFactory().createReasoner(uberon);
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	}

	@AfterClass
	def disposeReasoner(): Unit = {
			reasoner.dispose();
	}

}

class TestQueryExpander {

	@Test
	def testQueryExpander(): Unit = {
			val expression = <ObjectSomeValuesFrom><ObjectProperty abbreviatedIRI="part_of:"/><Class abbreviatedIRI="head:"/></ObjectSomeValuesFrom>;
			val expressionText = EscapeStr.stringEsc(expression.toString());
			val query = """
					PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
					PREFIX owl: <http://www.w3.org/2002/07/owl#>
					PREFIX of: <http://purl.org/phenoscape/owl-filter/syntax#>
					PREFIX head: <http://purl.obolibrary.org/obo/UBERON_0000033>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					PREFIX has_part: <http://purl.obolibrary.org/obo/BFO_0000051>
					SELECT *
					WHERE
					{
					?organism has_part: ?part .
					?part rdf:type ?structure .
					?structure rdfs:subClassOf "%s"^^of:owx .
					}
					""".format(expressionText);
			val expander = new QueryExpander(TestQueryExpander.reasoner);
			println(query);
			println(expander.expandQueryString(query));
	}



}