package org.phenoscape.owlet

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.expression.OWLEntityChecker
import org.semanticweb.owlapi.model._

class StandaloneEntityChecker(prefixes: PartialFunction[String, String]) extends OWLEntityChecker {

  import StandaloneEntityChecker._

  private val factory = OWLManager.getOWLDataFactory

  override def getOWLClass(name: String): OWLClass = nameToIRI(name, prefixes).map(factory.getOWLClass).orNull

  override def getOWLObjectProperty(name: String): OWLObjectProperty = nameToIRI(name, prefixes).map(factory.getOWLObjectProperty).orNull

  //FIXME OWL API standalone expression parsing fails if both data property and object property validate; this prevents use of data properties
  override def getOWLDataProperty(name: String): OWLDataProperty = null //nameToIRI(name, prefixes).map(factory.getOWLDataProperty).orNull

  override def getOWLIndividual(name: String): OWLNamedIndividual = nameToIRI(name, prefixes).map(factory.getOWLNamedIndividual).orNull

  override def getOWLDatatype(name: String): OWLDatatype = nameToIRI(name, prefixes).map(factory.getOWLDatatype).orNull

  override def getOWLAnnotationProperty(name: String): OWLAnnotationProperty = nameToIRI(name, prefixes).map(factory.getOWLAnnotationProperty).orNull

}

object StandaloneEntityChecker {

  private val CURIE = "^([^:]*):(.*)$".r
  private val FullIRI = "^<(.+)>$".r

  def nameToIRI(name: String, prefixes: PartialFunction[String, String]): Option[IRI] = {
    println(name)
    println(prefixes)
    name match {
      case FullIRI(iri)         => Option(IRI.create(iri))
      case CURIE(prefix, local) => prefixes.lift(prefix).map(uri => IRI.create(s"$uri$local"))
      case _                    => None
    }
  }

}
