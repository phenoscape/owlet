package org.nescent.owl.filter

import org.apache.jena.riot.out.EscapeStr
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner

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
			val xmlExpression = <ObjectSomeValuesFrom><ObjectProperty abbreviatedIRI="part_of:"/><Class abbreviatedIRI="head:"/></ObjectSomeValuesFrom>;
			val xmlExpressionText = EscapeStr.stringEsc(xmlExpression.toString());
			val xmlQuery = """
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
					""".format(xmlExpressionText);
			val expander = new QueryExpander(TestQueryExpander.reasoner);
			println(xmlQuery);
			println(expander.expandQueryString(xmlQuery));
			
			val manchesterQuery = """
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
					?structure rdfs:subClassOf "part_of: some head:"^^of:omn .
					}
					"""
			  println(expander.expandQueryString(manchesterQuery));
	}



}