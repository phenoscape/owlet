package org.nescent.owl.filter

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.reasoner.OWLReasoner
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.util.FileManager
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.query.QueryExecutionFactory
import org.junit.Assert

class TestSPARQLQuery {

	@Test
	def testSPARQLQuery(): Unit = {
			val manager = OWLManager.createOWLOntologyManager();
			val owlStream = getClass().getClassLoader().getResourceAsStream("vsao.owl");
			val vsaoOWL = manager.loadOntologyFromOntologyDocument(owlStream);
			owlStream.close();
			val rdfStream = getClass().getClassLoader().getResourceAsStream("vsao.owl");
			val vsaoRDF = ModelFactory.createDefaultModel() ;
			vsaoRDF.read(rdfStream, null);
			rdfStream.close();
			val reasoner = new ElkReasonerFactory().createReasoner(vsaoOWL);
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			val queryText = """
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>
					PREFIX definition: <http://purl.obolibrary.org/obo/IAO_0000115>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					SELECT DISTINCT ?structure ?label ?definition
					WHERE
					{
					?structure rdfs:label ?label .
					?structure definition: ?definition .
					?structure rdfs:subClassOf "part_of: some axial_skeleton:"^^ow:omn .
					}
					""";
			val expander = new QueryExpander(reasoner);
			val query = QueryFactory.create(queryText);
			val expandedQuery = expander.expandQuery(query);
			val results = QueryExecutionFactory.create(expandedQuery, vsaoRDF).execSelect();
			var count = 0;
			while (results.hasNext()) {
			  count += 1;
			}
			Assert.assertEquals("Should get seven results", 7, count);
			reasoner.dispose();
	}

}
