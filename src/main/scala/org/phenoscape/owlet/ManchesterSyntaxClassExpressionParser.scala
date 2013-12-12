package org.phenoscape.owlet

import scala.collection.JavaConversions._
import scala.collection.Map
import scala.util.parsing.combinator.RegexParsers

import org.apache.log4j.Logger
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLDataProperty
import org.semanticweb.owlapi.model.OWLDatatype
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom
import org.semanticweb.owlapi.model.OWLObjectComplementOf
import org.semanticweb.owlapi.model.OWLObjectExactCardinality
import org.semanticweb.owlapi.model.OWLObjectHasSelf
import org.semanticweb.owlapi.model.OWLObjectHasValue
import org.semanticweb.owlapi.model.OWLObjectInverseOf
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality
import org.semanticweb.owlapi.model.OWLObjectMinCardinality
import org.semanticweb.owlapi.model.OWLObjectOneOf
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom
import org.semanticweb.owlapi.vocab.XSDVocabulary

object ManchesterSyntaxClassExpressionParser {

  def parse(expression: String): Option[OWLClassExpression] = {
    parse(expression, Map())
  }

  def parse(expression: String, prefixes: Map[String, String]): Option[OWLClassExpression] = {
    val parser = new ManchesterParser(prefixes)
    parser.parseExpression(expression)
  }

  private
  class ManchesterParser(prefixes: Map[String, String]) extends RegexParsers {

    private val factory = OWLManager.getOWLDataFactory
    // These are modified from http://www.w3.org/TR/2008/REC-rdf-sparql-query-20080115/
    val PN_CHARS_BASE = "[A-Z]|[a-z]|[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]|[\u10000-\uEFFFF]"
    val PN_CHARS_U = """%s|_""".format(PN_CHARS_BASE)
    val PN_CHARS = """%s|\-|[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040]""".format(PN_CHARS_U)
    val PN_PREFIX = """(%s)((%s|\.)*(%s))?""".format(PN_CHARS_BASE, PN_CHARS, PN_CHARS)
    val PN_LOCAL = """(%s|[0-9])((%s|\.)*(%s))?""".format(PN_CHARS_U, PN_CHARS, PN_CHARS)
    val PNAME_NS = """((%s)??):""".format(PN_PREFIX)
    val PNAME_LN = "%s(%s)*".format(PNAME_NS, PN_LOCAL)

    // From http://www.artefarita.com/journel/post/2013/05/23/An-IRI-pattern-for-Java:
    //def fullIRI: Parser[IRI] = "<(?:[a-z](?:[-a-z0-9\\+\\.])*:(?:\\/\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:])*@)?(?:\\[(?:(?:(?:[0-9a-f]{1,4}:){6}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|::(?:[0-9a-f]{1,4}:){5}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4}:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|v[0-9a-f]+[-a-z0-9\\._~!\\$&'\\(\\)\\*\\+,;=:]+)\\]|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}|(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=@])*)(?::[0-9]*)?(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))*)*|\\/(?:(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))*)*)?|(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))*)*|(?!(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@])))(?:\\?(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@])|[\\uE000-\\uF8FF\\uDC00\\uDB80-\\uDFFD\\uDBBF|\\uDC00\\uDBC0-\\uDFFD\\uDBFF\\/\\?])*)?(?:\\#(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@])|[\\/\\?])*)?|(?:\\/\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:])*@)?(?:\\[(?:(?:(?:[0-9a-f]{1,4}:){6}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|::(?:[0-9a-f]{1,4}:){5}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4}:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|v[0-9a-f]+[-a-z0-9\\._~!\\$&'\\(\\)\\*\\+,;=:]+)\\]|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}|(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=@])*)(?::[0-9]*)?(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))*)*|\\/(?:(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))*)*)?|(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=@])+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@]))*)*|(?!(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@])))(?:\\?(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@])|[\\uE000-\\uF8FF\\uDC00\\uDB80-\\uDFFD\\uDBBF|\\uDC00\\uDBC0-\\uDFFD\\uDBFF\\/\\?])*)?(?:\\#(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF\\uDC00\\uD800-\\uDFFD\\uD83F\\uDC00\\uD840-\\uDFFD\\uD87F\\uDC00\\uD880-\\uDFFD\\uD8BF\\uDC00\\uD8C0-\\uDFFD\\uD8FF\\uDC00\\uD900-\\uDFFD\\uD93F\\uDC00\\uD940-\\uDFFD\\uD97F\\uDC00\\uD980-\\uDFFD\\uD9BF\\uDC00\\uD9C0-\\uDFFD\\uD9FF\\uDC00\\uDA00-\\uDFFD\\uDA3F\\uDC00\\uDA40-\\uDFFD\\uDA7F\\uDC00\\uDA80-\\uDFFD\\uDABF\\uDC00\\uDAC0-\\uDFFD\\uDAFF\\uDC00\\uDB00-\\uDFFD\\uDB3F\\uDC00\\uDB44-\\uDFFD\\uDB7F!\\$&'\\(\\)\\*\\+,;=:@])|[\\/\\?])*)?)>".r ^^ (in => IRI.create(in.substring(1, in.length() - 1)))
    def fullIRI: Parser[IRI] = """<[^\s]+>""" .r ^^ (in => IRI.create(in.substring(1, in.length() - 1)))
      
    def abbreviatedIRI: Parser[IRI] = PNAME_LN.r ^^ (in => {
      val segments = in.split(":", 2)
      val localName = if (segments.size > 1) segments(1) else ""
      IRI.create(prefixes(segments(0)) + localName)
    })

    def iri: Parser[IRI] = fullIRI | abbreviatedIRI

    def nonNegativeInteger: Parser[Int] = zero | positiveInteger

    def positiveInteger = """[1-9]?[0-9]*""".r ^^ (_.toInt)

    def digits = "[0-9]+".r ^^ (_.toInt)

    def digit = "[0-9]".r ^^ (_.toInt)

    def zero = "0" ^^ (_.toInt)

    def classIRI: Parser[OWLClass] = iri ^^ factory.getOWLClass

    def objectPropertyIRI: Parser[OWLObjectProperty] = iri ^^ factory.getOWLObjectProperty

    def dataPropertyIRI: Parser[OWLDataProperty] = iri ^^ factory.getOWLDataProperty

    def individualIRI: Parser[OWLNamedIndividual] = iri ^^ factory.getOWLNamedIndividual

    def individualList: Parser[OWLObjectOneOf] = "{" ~> repsep(individualIRI, ",") <~ "}" ^^ (in => factory.getOWLObjectOneOf(in.toSet))

    def integerDatatype: Parser[OWLDatatype] = "integer" ^^ (in => factory.getIntegerOWLDatatype)

    def decimalDatatype: Parser[OWLDatatype] = "decimal" ^^ (in => factory.getOWLDatatype(XSDVocabulary.DECIMAL.getIRI))

    def floatDatatype: Parser[OWLDatatype] = "float" ^^ (in => factory.getFloatOWLDatatype)

    def stringDatatype: Parser[OWLDatatype] = "string" ^^ (in => factory.getOWLDatatype(XSDVocabulary.STRING.getIRI))

    def datatype: Parser[OWLDatatype] = datatypeIRI | integerDatatype | decimalDatatype | floatDatatype | stringDatatype

    def datatypeIRI: Parser[OWLDatatype] = iri ^^ (in => factory.getOWLDatatype(in))

    def inverseObjectProperty: Parser[OWLObjectInverseOf] = ("inverse" ~> objectPropertyIRI) ^^ (factory.getOWLObjectInverseOf(_))

    def objectPropertyExpression: Parser[OWLObjectPropertyExpression] = objectPropertyIRI | inverseObjectProperty

    def description: Parser[OWLClassExpression] = repsep(conjunction, "or") ^^ (in => in.size match {
      case 1 => in.head
      case _ => factory.getOWLObjectUnionOf(in.toSet)
    })

    def conjunction: Parser[OWLClassExpression] = //classIRI ~ "that" ~ opt("not") ~ restriction ~ rep("and" ~ opt("not") ~ restriction) |
      repsep(primary, "and") ^^ (in => in.size match {
        case 1 => in.head
        case _ => factory.getOWLObjectIntersectionOf(in.toSet)
      })

    def negation: Parser[OWLObjectComplementOf] = "not" ~> (restriction | atomic) ^^ factory.getOWLObjectComplementOf

    def primary: Parser[OWLClassExpression] = negation | restriction | atomic

    def someValuesFrom: Parser[OWLObjectSomeValuesFrom] = objectPropertyExpression ~ ("some" ~> primary) ^^ {
      case prop ~ filler => factory.getOWLObjectSomeValuesFrom(prop, filler)
    }

    def allValuesFrom: Parser[OWLObjectAllValuesFrom] = objectPropertyExpression ~ ("only" ~> primary) ^^ {
      case prop ~ filler => factory.getOWLObjectAllValuesFrom(prop, filler)
    }

    def objectHasValue: Parser[OWLObjectHasValue] = objectPropertyExpression ~ ("value" ~> individualIRI) ^^ {
      case prop ~ filler => factory.getOWLObjectHasValue(prop, filler)
    }

    def objectHasSelf: Parser[OWLObjectHasSelf] = objectPropertyExpression <~ "Self" ^^ factory.getOWLObjectHasSelf

    def objectMinCardinality: Parser[OWLObjectMinCardinality] = objectPropertyExpression ~ ("min" ~> nonNegativeInteger) ~ opt(primary) ^^ {
      case prop ~ cardinality ~ Some(filler) => factory.getOWLObjectMinCardinality(cardinality, prop, filler)
      case prop ~ cardinality ~ None => factory.getOWLObjectMinCardinality(cardinality, prop)
    }

    def objectMaxCardinality: Parser[OWLObjectMaxCardinality] = objectPropertyExpression ~ ("max" ~> nonNegativeInteger) ~ opt(primary) ^^ {
      case prop ~ cardinality ~ Some(filler) => factory.getOWLObjectMaxCardinality(cardinality, prop, filler)
      case prop ~ cardinality ~ None => factory.getOWLObjectMaxCardinality(cardinality, prop)
    }

    def objectExactCardinality: Parser[OWLObjectExactCardinality] = objectPropertyExpression ~ ("exactly" ~> nonNegativeInteger) ~ opt(primary) ^^ {
      case prop ~ cardinality ~ Some(filler) => factory.getOWLObjectExactCardinality(cardinality, prop, filler)
      case prop ~ cardinality ~ None => factory.getOWLObjectExactCardinality(cardinality, prop)
    }

    //dataPropertyExpression ~ "some" ~ dataPrimary |
    //dataPropertyExpression ~ "only" ~ dataPrimary |
    //				dataPropertyExpression ~ "value" ~ literal |
    //				dataPropertyExpression ~ "min" ~ nonNegativeInteger ~ opt(dataPrimary) |
    //				dataPropertyExpression ~ "max" ~ nonNegativeInteger ~ opt(dataPrimary) |
    //				dataPropertyExpression ~ "exactly" ~ nonNegativeInteger ~ opt(dataPrimary)

    def restriction: Parser[OWLClassExpression] = someValuesFrom | allValuesFrom | objectHasValue | objectHasSelf | objectMinCardinality | objectMaxCardinality | objectExactCardinality

    def atomic: Parser[OWLClassExpression] = classIRI | individualList | "(" ~> description <~ ")"

    def parseExpression(expression: String): Option[OWLClassExpression] = {
      parseAll(description, expression) match {
        case Success(result, remainder) => Option(result)
        case NoSuccess(message, remainder) => {
          logger.error("Failed to parse class expression: " + message)
          None
        }
      }
    }

    lazy val logger = Logger.getLogger(ManchesterSyntaxClassExpressionParser.getClass)

  }

}
