package org.phenoscape.owlet

import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.rdf.model.ModelFactory
import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.InferenceType
import utest._

import scala.jdk.CollectionConverters._
import scala.util.Using

object TestSPARQLQuery extends TestSuite {

  private val manager = OWLManager.createOWLOntologyManager()
  private val vsao = Using.resource(this.getClass.getClassLoader.getResourceAsStream("vsao.owl")) { vsaoStream =>
    manager.loadOntologyFromOntologyDocument(vsaoStream)
  }
  private val reasoner = new ElkReasonerFactory().createReasoner(vsao)
  reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

  override def utestAfterAll(): Unit = reasoner.dispose()

  val tests: Tests = Tests {
    test("Test SPARQL query using filter") - {
      val vsaoRDF = Using.resource(this.getClass.getClassLoader.getResourceAsStream("vsao.owl")) { stream =>
        ModelFactory.createDefaultModel().read(stream, null)
      }
      val queryText =
        """
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
      """
      val owlet = new Owlet(TestSPARQLQuery.reasoner)
      val query = QueryFactory.create(queryText)
      val unexpandedResults = QueryExecutionFactory.create(query, vsaoRDF).execSelect()
      //Shouldn't get any results before expansion
      assert(!unexpandedResults.hasNext)
      val expandedQuery = owlet.expandQuery(query, false)
      val results = QueryExecutionFactory.create(expandedQuery, vsaoRDF).execSelect()
      assert(7 == results.asScala.length)
    }
    test("Test SPARQL query using values") - {
      val vsaoRDF = Using.resource(this.getClass.getClassLoader.getResourceAsStream("vsao.owl")) { stream =>
        ModelFactory.createDefaultModel().read(stream, null)
      }
      val queryText =
        """
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
            """
      val owlet = new Owlet(TestSPARQLQuery.reasoner)
      val query = QueryFactory.create(queryText)
      val unexpandedResults = QueryExecutionFactory.create(query, vsaoRDF).execSelect()
      // Shouldn't get any results before expansion
      assert(!unexpandedResults.hasNext)
      val expandedQuery = owlet.expandQuery(query, true)
      val results = QueryExecutionFactory.create(expandedQuery, vsaoRDF).execSelect()
      assert(7 == results.asScala.length)
    }
  }

}
