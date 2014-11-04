package org.phenoscape.owlet

import org.junit.Assert
import org.junit.Test
import org.semanticweb.elk.owlapi.ElkReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.reasoner.InferenceType
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.rdf.model.ModelFactory
import scala.collection.JavaConversions._
import org.junit.BeforeClass
import org.junit.AfterClass
import org.semanticweb.owlapi.reasoner.OWLReasoner

object TestSPARQLQuery {

  var reasoner: OWLReasoner = null

  @BeforeClass
  def setupReasoner(): Unit = {
    val manager = OWLManager.createOWLOntologyManager()
    val vsaoStream = this.getClass.getClassLoader.getResourceAsStream("vsao.owl")
    val vsao = manager.loadOntologyFromOntologyDocument(vsaoStream)
    vsaoStream.close()
    reasoner = new ElkReasonerFactory().createReasoner(vsao)
    reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
  }

  @AfterClass
  def disposeReasoner(): Unit = {
    reasoner.dispose()
  }

}

class TestSPARQLQuery {

  @Test
  def testSPARQLQueryUsingFilter(): Unit = {
    val rdfStream = this.getClass.getClassLoader.getResourceAsStream("vsao.owl")
    val vsaoRDF = ModelFactory.createDefaultModel()
    vsaoRDF.read(rdfStream, null)
    rdfStream.close()
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
					"""
    val owlet = new Owlet(TestSPARQLQuery.reasoner)
    val query = QueryFactory.create(queryText)
    val unexpandedResults = QueryExecutionFactory.create(query, vsaoRDF).execSelect()
    Assert.assertFalse("Shouldn't get any results before expansion", unexpandedResults.hasNext)
    val expandedQuery = owlet.expandQuery(query)
    val results = QueryExecutionFactory.create(expandedQuery, vsaoRDF).execSelect()
    Assert.assertEquals("Should get seven results", 7, results.length)
  }

  //@Test
  def testSPARQLQueryUsingOwletGraph(): Unit = {
    val owlet = new Owlet(TestSPARQLQuery.reasoner)
    val basicQuery = """
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>
					PREFIX definition: <http://purl.obolibrary.org/obo/IAO_0000115>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					SELECT DISTINCT ?structure
					WHERE
					{
					?structure rdfs:subClassOf "part_of: some axial_skeleton:"^^ow:omn .
					}
					"""
    Assert.assertEquals("Should get nine results", 9, owlet.performSPARQLQuery(QueryFactory.create(basicQuery)).length)

    val queryWithValues = """
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>
					PREFIX definition: <http://purl.obolibrary.org/obo/IAO_0000115>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					SELECT DISTINCT ?structure
					WHERE
					{
					VALUES ?structure { <http://purl.obolibrary.org/obo/VSAO_0000093> }
					?structure rdfs:subClassOf "part_of: some axial_skeleton:"^^ow:omn .
					}
					"""
    Assert.assertEquals("Should get one result", 1, owlet.performSPARQLQuery(QueryFactory.create(queryWithValues)).length)

    val queryWithStar = """
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>
					PREFIX definition: <http://purl.obolibrary.org/obo/IAO_0000115>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					SELECT DISTINCT ?structure
					WHERE
					{
					?structure rdfs:subClassOf* "part_of: some axial_skeleton:"^^ow:omn .
					}
					"""
    Assert.assertEquals("Should get ten results (query engine includes expression as a result)", 10, owlet.performSPARQLQuery(QueryFactory.create(queryWithStar)).length)

    val queryWithPlus = """
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>
					PREFIX definition: <http://purl.obolibrary.org/obo/IAO_0000115>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					SELECT DISTINCT ?structure
					WHERE
					{
					?structure rdfs:subClassOf+ "part_of: some axial_skeleton:"^^ow:omn .
					}
					"""
    Assert.assertEquals("Should get nine results", 9, owlet.performSPARQLQuery(QueryFactory.create(queryWithPlus)).length)

    val queryWithQuestionMark = """
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX axial_skeleton: <http://purl.obolibrary.org/obo/VSAO_0000056>
					PREFIX definition: <http://purl.obolibrary.org/obo/IAO_0000115>
					PREFIX part_of: <http://purl.obolibrary.org/obo/BFO_0000050>
					SELECT DISTINCT ?structure
					WHERE
					{
					?structure rdfs:subClassOf? "part_of: some axial_skeleton:"^^ow:omn .
					}
					"""
    Assert.assertEquals("Should get ten results (query engine includes expression as a result)", 10, owlet.performSPARQLQuery(QueryFactory.create(queryWithQuestionMark)).length)

    val queryWithAlternative = """
					PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
					PREFIX owl: <http://www.w3.org/2002/07/owl#>
					PREFIX ow: <http://purl.org/phenoscape/owlet/syntax#>
					PREFIX skeletal_element: <http://purl.obolibrary.org/obo/VSAO_0000128>
					PREFIX bone_tissue: <http://purl.obolibrary.org/obo/VSAO_0000047>
					PREFIX has_part: <http://purl.obolibrary.org/obo/BFO_0000051>
					SELECT DISTINCT ?structure
					WHERE
					{
					?structure rdfs:subClassOf|owl:equivalentClass "skeletal_element: and (has_part: some bone_tissue:)"^^ow:omn .
					}
					"""
    Assert.assertEquals("Should get nineteen results", 19, owlet.performSPARQLQuery(QueryFactory.create(queryWithAlternative)).length)
  }

}
