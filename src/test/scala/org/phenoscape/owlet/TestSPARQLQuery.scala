package org.phenoscape.owlet

import org.junit.Assert
import org.junit.Test
import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.InferenceType

import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.rdf.model.ModelFactory

class TestSPARQLQuery {

	@Test
	def testSPARQLQuery(): Unit = {
			val manager = OWLManager.createOWLOntologyManager()
			val owlStream = this.getClass.getClassLoader.getResourceAsStream("vsao.owl")
			val vsaoOWL = manager.loadOntologyFromOntologyDocument(owlStream)
			owlStream.close()
			val rdfStream = this.getClass.getClassLoader.getResourceAsStream("vsao.owl")
			val vsaoRDF = ModelFactory.createDefaultModel() 
			vsaoRDF.read(rdfStream, null)
			rdfStream.close()
			val reasoner = new ElkReasonerFactory().createReasoner(vsaoOWL)
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
			val queryText = """
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX ow: <http://purl.org/owlet/#>
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
					"""
			val expander = new QueryExpander(reasoner)
			val query = QueryFactory.create(queryText)
			val unexpandedResults = QueryExecutionFactory.create(query, vsaoRDF).execSelect()
			Assert.assertFalse("Shouldn't get any results before expansion", unexpandedResults.hasNext)
			val expandedQuery = expander.expandQuery(query)
			val results = QueryExecutionFactory.create(expandedQuery, vsaoRDF).execSelect()
			var count = 0
			while (results.hasNext) {
				results.next()
				count += 1
			}
			Assert.assertEquals("Should get seven results", 7, count)
			reasoner.dispose()
	}

}
