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

	@Test
	def testforNullPointerExceptionWithPropertyPaths(): Unit = {
			val query = """
					PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX dc: <http://purl.org/dc/elements/1.1/>
					PREFIX obo: <http://purl.obolibrary.org/obo/>
					PREFIX ps: <http://purl.org/phenoscape/vocab/>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX StandardState: <http://purl.obolibrary.org/obo/CDAO_0000045>
					PREFIX has_character: <http://purl.obolibrary.org/obo/CDAO_0000142>
					PREFIX has_state: <http://purl.obolibrary.org/obo/CDAO_0000184>
					PREFIX belongs_to_tu: <http://purl.obolibrary.org/obo/CDAO_0000191>
					PREFIX has_external_reference: <http://purl.obolibrary.org/obo/CDAO_0000164>
					PREFIX HasNumberOf: <http://purl.obolibrary.org/obo/PATO_0001555>
					PREFIX Count: <http://purl.obolibrary.org/obo/PATO_0000070>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					PREFIX LimbFin: <http://purl.obolibrary.org/obo/UBERON_0004708>
					PREFIX Sarcopterygii: <http://purl.obolibrary.org/obo/VTO_0001464>

					SELECT DISTINCT ?character ?entity ?entity_label ?state ?state_label ?matrix_label ?taxon ?taxon_label
					FROM <http://kb.phenoscape.org/>
					WHERE
					{
					?character <http://example.org/entity_term> ?entity .
					?character <http://example.org/quality_term> ?quality .
					FILTER(?quality IN (Count:, HasNumberOf:))
					?entity rdfs:subClassOf "part_of: some LimbFin:"^^ow:omn .
					?eq rdfs:subClassOf ?character .
					?pheno_instance rdf:type ?eq .
					?state ps:denotes_exhibiting ?pheno_instance .
					?state rdf:type StandardState: .
					?state dc:description ?state_label .
					?entity rdfs:label ?entity_label .
					?quality rdfs:label ?quality_label .
					?matrix has_character: ?matrix_char .
					?matrix rdfs:label ?matrix_label .
					?matrix_char ps:may_have_state_value ?state .
					?cell has_state: ?state .
					?cell belongs_to_tu: ?otu .
					?otu has_external_reference: ?taxon .
					?taxon rdfs:label ?taxon_label .
					?taxon rdfs:subClassOf* Sarcopterygii: .
					}""";
			val expander = new QueryExpander(TestQueryExpander.reasoner);
			val expandedQuery = expander.expandQueryString(query);
			println(expandedQuery);
	}

}