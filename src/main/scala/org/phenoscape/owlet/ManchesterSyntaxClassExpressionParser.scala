package org.phenoscape.owlet

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser
import org.semanticweb.owlapi.model._

import scala.collection.Map
import scala.jdk.CollectionConverters._
import scala.util.Try

object ManchesterSyntaxClassExpressionParser {

  def parse(expression: String): Either[String, OWLClassExpression] = parse(expression, Map[String, String]())

  def parse(expression: String, prefixes: Map[String, String]): Either[String, OWLClassExpression] = {
    val checker = new StandaloneEntityChecker(prefixes)
    val parser = new ManchesterOWLSyntaxClassExpressionParser(OWLManager.getOWLDataFactory, checker)
    Try(parser.parse(expression)).toEither.left.map(_.getMessage)
  }

  def parse(expression: String, prefixes: java.util.Map[String, String]): Either[String, OWLClassExpression] =
    parse(expression, prefixes.asScala)

  def parseIRI(input: String, prefixes: Map[String, String] = Map.empty): Either[String, IRI] =
    StandaloneEntityChecker.nameToIRI(input, prefixes) match {
      case Some(iri) => Right(iri)
      case None      => Left("Invalid IRI")
    }

  def parseIRI(input: String, prefixes: java.util.Map[String, String]): Either[String, IRI] =
    parseIRI(input, prefixes.asScala)

}
