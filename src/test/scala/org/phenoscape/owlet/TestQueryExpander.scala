package org.phenoscape.owlet

import org.apache.jena.riot.out.EscapeStr
import org.junit.AfterClass
import org.junit.Assert
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
			val vsaoStream = getClass().getClassLoader().getResourceAsStream("vsao.owl");
			val vsao = manager.loadOntologyFromOntologyDocument(vsaoStream);
			reasoner = new ElkReasonerFactory().createReasoner(vsao);
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
			val expander = new QueryExpander(TestQueryExpander.reasoner);

			val xmlExpression = <ObjectSomeValuesFrom><ObjectProperty abbreviatedIRI="part_of:"/><Class abbreviatedIRI="axial_skeleton:"/></ObjectSomeValuesFrom>;
			val xmlExpressionText = EscapeStr.stringEsc(xmlExpression.toString());
			val xmlQuery = """
					PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
					PREFIX owl: <http://www.w3.org/2002/07/owl#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX vsao: <http://purl.obolibrary.org/obo/VSAO_>
					PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					PREFIX has_part: <http://purl.obolibrary.org/obo/BFO_0000051>
					SELECT *
					WHERE
					{
					?organism has_part: ?part .
					?part rdf:type ?structure .
					?structure rdfs:subClassOf "%s"^^ow:owx .
					}
					""".format(xmlExpressionText);

			println(xmlQuery);
			expander.expandQueryString(xmlQuery);

			val manchesterQuery = """
					PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
					PREFIX owl: <http://www.w3.org/2002/07/owl#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX vsao: <http://purl.obolibrary.org/obo/VSAO_>
					PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					PREFIX has_part: <http://purl.obolibrary.org/obo/BFO_0000051>
					SELECT *
					WHERE
					{
					?organism has_part: ?part .
					?part rdf:type ?structure .
					?structure rdfs:subClassOf "part_of: some axial_skeleton:"^^ow:omn .
					}
					""";
			val expandedQuery = expander.expandQueryString(manchesterQuery);
			Assert.assertTrue("Filter should contain term with identifier", expandedQuery.contains("0000093"));
			Assert.assertTrue("Filter should contain term with identifier", expandedQuery.contains("0000049"));
			Assert.assertTrue("Filter should contain term with identifier", expandedQuery.contains("0000183"));
			Assert.assertTrue("Filter should contain term with identifier", expandedQuery.contains("0000185"));
			Assert.assertTrue("Filter should contain term with identifier", expandedQuery.contains("0000149"));
			Assert.assertTrue("Filter should contain term with identifier", expandedQuery.contains("0000082"));
			Assert.assertTrue("Filter should contain term with identifier", expandedQuery.contains("0000184"));

	}

}