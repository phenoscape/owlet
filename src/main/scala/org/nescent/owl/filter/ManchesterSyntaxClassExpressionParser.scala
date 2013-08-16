package org.nescent.owl.filter

import scala.collection.JavaConversions._
import org.semanticweb.owlapi.apibinding.OWLManager
import org.obolibrary.macro.ManchesterSyntaxTool
import scala.xml.XML
import scala.collection.Set
import scala.collection.Map
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.util.DefaultPrefixManager
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.IRI

object ManchesterSyntaxClassExpressionParser {
  
  val factory = OWLManager.getOWLDataFactory();
  
  def parse(expression: String): OWLClassExpression = {
			parse(expression, Map());
	}

	def parse(expression: String, prefixes: Map[String, String]): OWLClassExpression = {
			val prefixManager = new DefaultPrefixManager();
			//prefixes.foreach {case (key, value) => prefixManager.setPrefix(key + ":", value)};
			val manager = OWLManager.createOWLOntologyManager();
			val ontology = manager.createOntology();
			manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(IRI.create("http://example.org/Woman"))));
			manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(IRI.create("http://example.org/Parent"))));
			//val entityChecker = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(new ManchesterOWLSyntaxPrefixNameShortFormProvider(prefixManager)));
			//val entityChecker = Checker;
			//val manchester = new ManchesterOWLSyntaxClassExpressionParser(factory, entityChecker);
			val manchester = new ManchesterSyntaxTool(ontology, Set[OWLOntology](), false);
			manchester.parseManchesterExpression(expression);


			return null;
	}
	
	
	
//	object Checker extends OWLEntityChecker {
//
//		def getOWLAnnotationProperty(name: String): OWLAnnotationProperty = {
//				println("Asked for annotation property: " + name);
//				factory.getOWLAnnotationProperty(IRI.create("http://example.org/annotation"));
//		} 
//		def	getOWLClass(name: String): OWLClass = {
//				println("Asked for class: " + name);
//				factory.getOWLClass(IRI.create("http://example.org/class"));
//		} 
//		def	getOWLDataProperty(name: String): OWLDataProperty = {
//				println("Asked for data property: " + name);
//				factory.getOWLDataProperty(IRI.create("http://example.org/dataproperty"));
//		} 
//		def	getOWLDatatype(name: String): OWLDatatype = {
//				println("Asked for datatype: " + name);
//				factory.getOWLDatatype(IRI.create("http://example.org/datatype"));
//		} 
//		def	getOWLIndividual(name: String): OWLNamedIndividual = {
//				println("Asked for individual: " + name);
//				factory.getOWLNamedIndividual(IRI.create("http://example.org/individual"));
//		} 
//
//		def getOWLObjectProperty(name: String) : OWLObjectProperty = {
//				factory.getOWLObjectProperty(IRI.create("http://example.org/objectproperty"));
//		}
//
//	}

}