package org.phenoscape.owlet

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser
import org.semanticweb.owlapi.model._
import scalaz._

import scala.collection.JavaConverters._
import scala.collection.Map

object ManchesterSyntaxClassExpressionParser {

  def parse(expression: String): Validation[String, OWLClassExpression] = parse(expression, Map[String, String]())

  def parse(expression: String, prefixes: Map[String, String]): Validation[String, OWLClassExpression] = {
    val checker = new StandaloneEntityChecker(prefixes)
    val parser = new ManchesterOWLSyntaxClassExpressionParser(OWLManager.getOWLDataFactory, checker)
    Validation.fromTryCatchNonFatal(parser.parse(expression)).leftMap(_.getMessage)
  }

  def parse(expression: String, prefixes: java.util.Map[String, String]): Validation[String, OWLClassExpression] =
    parse(expression, prefixes.asScala)

  def parseIRI(input: String, prefixes: Map[String, String] = Map.empty): Validation[String, IRI] = {
    StandaloneEntityChecker.nameToIRI(input, prefixes) match {
      case Some(iri) => Validation.success(iri)
      case None      => Validation.failure("Invalid IRI")
    }
  }

  def parseIRI(input: String, prefixes: java.util.Map[String, String]): Validation[String, IRI] =
    parseIRI(input, prefixes.asScala)

}
