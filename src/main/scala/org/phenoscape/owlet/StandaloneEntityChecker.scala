package org.phenoscape.owlet

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.expression.OWLEntityChecker
import org.semanticweb.owlapi.model._
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.vocab.{OWL2Datatype, OWLRDFVocabulary, XSDVocabulary}

import scala.jdk.CollectionConverters._

class StandaloneEntityChecker(prefixes: PartialFunction[String, String], ontologyOpt: Option[OWLOntology] = None) extends OWLEntityChecker {

  import StandaloneEntityChecker._

  private val factory = OWLManager.getOWLDataFactory

  private val knownEntities: Option[KnownEntities] = ontologyOpt.map { ontology =>
    val builtInDatatypes = XSDVocabulary.values.map(_.getIRI).toSet ++ OWL2Datatype.values.map(_.getIRI) + OWLRDFVocabulary.RDFS_LITERAL.getIRI + OWLRDFVocabulary.RDF_XML_LITERAL.getIRI
    KnownEntities(
      ontology.getClassesInSignature(Imports.INCLUDED).asScala.toSet,
      ontology.getIndividualsInSignature(Imports.INCLUDED).asScala.toSet,
      ontology.getObjectPropertiesInSignature(Imports.INCLUDED).asScala.toSet,
      ontology.getDataPropertiesInSignature(Imports.INCLUDED).asScala.toSet,
      ontology.getAnnotationPropertiesInSignature(Imports.INCLUDED).asScala.toSet,
      ontology.getDatatypesInSignature(Imports.INCLUDED).asScala.map(_.getIRI).toSet ++ builtInDatatypes
    )

  }

  override def getOWLClass(name: String): OWLClass =
    nameToIRI(name, prefixes).map { iri =>
      val cls = factory.getOWLClass(iri)
      knownEntities.map { known =>
        if (known.classes(cls)) cls else null
      }.getOrElse(cls)
    }.orNull

  override def getOWLObjectProperty(name: String): OWLObjectProperty =
    nameToIRI(name, prefixes).map { iri =>
      val prop = factory.getOWLObjectProperty(iri)
      knownEntities.map { known =>
        if (known.objectProperties(prop)) prop else null
      }.getOrElse(prop)
    }.orNull

  override def getOWLDataProperty(name: String): OWLDataProperty =
    nameToIRI(name, prefixes).map { iri =>
      val prop = factory.getOWLDataProperty(iri)
      knownEntities.map { known =>
        if (known.dataProperties(prop)) prop else null
      }.orNull // Can't parse data properties without ontology
    }.orNull

  override def getOWLIndividual(name: String): OWLNamedIndividual =
    nameToIRI(name, prefixes).map { iri =>
      val ind = factory.getOWLNamedIndividual(iri)
      knownEntities.map { known =>
        if (known.individuals(ind)) ind else null
      }.getOrElse(ind)
    }.orNull

  override def getOWLDatatype(name: String): OWLDatatype =
    nameToIRI(name, prefixes).map { iri =>
      val dt = factory.getOWLDatatype(iri)
      knownEntities.map { known =>
        if (known.datatypes(iri)) dt else null
      }.getOrElse(dt)
    }.orNull

  override def getOWLAnnotationProperty(name: String): OWLAnnotationProperty =
    nameToIRI(name, prefixes).map { iri =>
      val prop = factory.getOWLAnnotationProperty(iri)
      knownEntities.map { known =>
        if (known.annotationProperties(prop)) prop else null
      }.getOrElse(prop)
    }.orNull

  private final case class KnownEntities(classes: Set[OWLClass],
                                         individuals: Set[OWLNamedIndividual],
                                         objectProperties: Set[OWLObjectProperty],
                                         dataProperties: Set[OWLDataProperty],
                                         annotationProperties: Set[OWLAnnotationProperty],
                                         datatypes: Set[IRI])

}

object StandaloneEntityChecker {

  private val CURIE = "^([^:]*):(.*)$".r
  private val FullIRI = "^<(.+)>$".r

  def nameToIRI(name: String, prefixes: PartialFunction[String, String]): Option[IRI] = name match {
    case FullIRI(iri)         => Option(IRI.create(iri))
    case CURIE(prefix, local) => prefixes.lift(prefix).map(uri => IRI.create(s"$uri$local"))
    case _                    => None
  }

}
