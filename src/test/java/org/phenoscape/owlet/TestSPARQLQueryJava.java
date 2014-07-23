package org.phenoscape.owlet;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestSPARQLQueryJava {

	@Test
	public void testSPARQLQuery() throws OWLOntologyCreationException, IOException {
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final InputStream owlStream = this.getClass().getClassLoader().getResourceAsStream("vsao.owl");
		final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(owlStream);
		owlStream.close();
		final OWLReasoner reasoner = new ElkReasonerFactory().createReasoner(ontology);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		final InputStream rdfStream = this.getClass().getClassLoader().getResourceAsStream("vsao.owl");
		final Model rdfModel = ModelFactory.createDefaultModel();
		rdfModel.read(rdfStream, null);
		rdfStream.close();
		final String queryText = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>\n"
				+ "PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>\n"
				+ "PREFIX definition: <http://purl.obolibrary.org/obo/IAO_0000115>\n"
				+ "PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>\n"
				+ "SELECT DISTINCT ?structure ?label ?definition\n"
				+ "WHERE\n"
				+ "{\n"
				+ "?structure rdfs:label ?label .\n"
				+ "?structure definition: ?definition .\n"
				+ "?structure rdfs:subClassOf \"part_of: some axial_skeleton:\"^^ow:omn .\n"
				+ "}";

		final Owlet owlet = new Owlet(reasoner);
		final Query query = QueryFactory.create(queryText);
		final Query expandedQuery = owlet.expandQuery(query);
		@SuppressWarnings("unused")
		final ResultSet results = QueryExecutionFactory.create(expandedQuery, rdfModel).execSelect();
	}

}
