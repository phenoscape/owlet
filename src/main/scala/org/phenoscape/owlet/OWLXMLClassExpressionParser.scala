package org.phenoscape.owlet

import scala.collection.JavaConversions._
import scala.collection.Map
import scala.xml.Elem
import scala.xml.XML
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLAnonymousIndividual
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLClassExpression
import org.semanticweb.owlapi.model.OWLIndividual
import org.semanticweb.owlapi.model.OWLNamedIndividual
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom
import org.semanticweb.owlapi.model.OWLObjectComplementOf
import org.semanticweb.owlapi.model.OWLObjectExactCardinality
import org.semanticweb.owlapi.model.OWLObjectHasSelf
import org.semanticweb.owlapi.model.OWLObjectHasValue
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf
import org.semanticweb.owlapi.model.OWLObjectInverseOf
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality
import org.semanticweb.owlapi.model.OWLObjectMinCardinality
import org.semanticweb.owlapi.model.OWLObjectOneOf
import org.semanticweb.owlapi.model.OWLObjectProperty
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom
import org.semanticweb.owlapi.model.OWLObjectUnionOf

object OWLXMLClassExpressionParser {

	val factory = OWLManager.getOWLDataFactory();

	; case class ObjectRestriction(property: OWLObjectPropertyExpression, filler: Option[OWLClassExpression]);

	def parse(expression: String): Option[OWLClassExpression] = {
			parse(expression, Map());
	}

	def parse(expression: String, prefixes: Map[String, String]): Option[OWLClassExpression] = {
			val expressionXML = XML.loadString(expression);
			Option(parseClassExpression(expressionXML, prefixes));			
	}

	def parseClassExpression(element: Elem, prefixes: Map[String, String]): OWLClassExpression = {
			element match {
			case <Class/> => parseClass(element, prefixes);
			case <ObjectIntersectionOf>{_*}</ObjectIntersectionOf> => parseObjectIntersectionOf(element, prefixes);
			case <ObjectUnionOf>{_*}</ObjectUnionOf> => parseObjectUnionOf(element, prefixes);
			case <ObjectComplementOf>{_*}</ObjectComplementOf> => parseObjectComplementOf(element, prefixes);
			case <ObjectOneOf>{_*}</ObjectOneOf> => parseObjectOneOf(element, prefixes);
			case <ObjectSomeValuesFrom>{_*}</ObjectSomeValuesFrom> => parseObjectSomeValuesFrom(element, prefixes);
			case <ObjectAllValuesFrom>{_*}</ObjectAllValuesFrom> => parseObjectAllValuesFrom(element, prefixes);
			case <ObjectHasValue>{_*}</ObjectHasValue> => parseObjectHasValue(element, prefixes);
			case <ObjectHasSelf>{_*}</ObjectHasSelf> => parseObjectHasSelf(element, prefixes);
			case <ObjectMinCardinality>{_*}</ObjectMinCardinality> => parseObjectMinCardinality(element, prefixes);
			case <ObjectMaxCardinality>{_*}</ObjectMaxCardinality> => parseObjectMaxCardinality(element, prefixes);
			case <ObjectExactCardinality>{_*}</ObjectExactCardinality> => parseObjectExactCardinality(element, prefixes);
			case <DataSomeValuesFrom>{_*}</DataSomeValuesFrom> => null;
			case <DataAllValuesFrom>{_*}</DataAllValuesFrom> => null;
			case <DataHasValue>{_*}</DataHasValue> => null;
			case <DataMinCardinality>{_*}</DataMinCardinality> => null;
			case <DataMaxCardinality>{_*}</DataMaxCardinality> => null;
			case <DataExactCardinality>{_*}</DataExactCardinality> => null;
			case _ => null;
			}
	}

	def parseClass(element: Elem, prefixes: Map[String, String]): OWLClass = {
			factory.getOWLClass(parseIRI(element, prefixes));
	}

	def parseObjectIntersectionOf(element: Elem, prefixes: Map[String, String]): OWLObjectIntersectionOf = {
			val classes = children(element).map(parseClassExpression(_, prefixes));
			factory.getOWLObjectIntersectionOf(classes.toSet);
	}

	def parseObjectUnionOf(element: Elem, prefixes: Map[String, String]): OWLObjectUnionOf = {
			val classes = children(element).map(parseClassExpression(_, prefixes));
			factory.getOWLObjectUnionOf(classes.toSet);
	}

	def parseObjectSomeValuesFrom(element: Elem, prefixes: Map[String, String]): OWLObjectSomeValuesFrom = {
			def struct = parseObjectRestriction(element, prefixes);
			factory.getOWLObjectSomeValuesFrom(struct.property, struct.filler.get);
	}

	def parseObjectAllValuesFrom(element: Elem, prefixes: Map[String, String]): OWLObjectAllValuesFrom = {
			def struct = parseObjectRestriction(element, prefixes);
			factory.getOWLObjectAllValuesFrom(struct.property, struct.filler.get);
	}

	def parseObjectRestriction(element: Elem, prefixes: Map[String, String]): ObjectRestriction = {
			val childElements = children(element);
			val property = parsePropertyExpression(childElements(0), prefixes);
			val filler = if (childElements.size > 1) {
				Option(parseClassExpression(childElements(1), prefixes));
			} else {
				None;
			}
			ObjectRestriction(property, filler);
	}

	def parseObjectComplementOf(element: Elem, prefixes: Map[String, String]): OWLObjectComplementOf = {
			factory.getOWLObjectComplementOf(parseClassExpression(children(element).head, prefixes));
	}

	def parseObjectOneOf(element: Elem, prefixes: Map[String, String]): OWLObjectOneOf = {
			val individuals = children(element).map(parseIndividual(_, prefixes));
			factory.getOWLObjectOneOf(individuals.toSet);
	}

	def parseObjectHasValue(element: Elem, prefixes: Map[String, String]): OWLObjectHasValue = {
			val childElements = children(element);
			val property = parsePropertyExpression(childElements(0), prefixes);
			val filler = parseIndividual(childElements(1), prefixes);
			factory.getOWLObjectHasValue(property, filler);
	}

	def parseObjectHasSelf(element: Elem, prefixes: Map[String, String]): OWLObjectHasSelf = {
			val property = parsePropertyExpression(children(element).head, prefixes);
			factory.getOWLObjectHasSelf(property);
	}

	def parseObjectMinCardinality(element: Elem, prefixes: Map[String, String]): OWLObjectMinCardinality = {
			val restriction = parseObjectRestriction(element, prefixes);
			val cardinality = (element \ "@cardinality").head.text.toInt;
			restriction match {
			case ObjectRestriction(property, Some(filler)) => factory.getOWLObjectMinCardinality(cardinality, property, filler);
			case ObjectRestriction(property, None) => factory.getOWLObjectMinCardinality(cardinality, property);
			}
	}

	def parseObjectMaxCardinality(element: Elem, prefixes: Map[String, String]): OWLObjectMaxCardinality = {
			val restriction = parseObjectRestriction(element, prefixes);
			val cardinality = (element \ "@cardinality").head.text.toInt;
			restriction match {
			case ObjectRestriction(property, Some(filler)) => factory.getOWLObjectMaxCardinality(cardinality, property, filler);
			case ObjectRestriction(property, None) => factory.getOWLObjectMaxCardinality(cardinality, property);
			}
	}

	def parseObjectExactCardinality(element: Elem, prefixes: Map[String, String]): OWLObjectExactCardinality = {
			val restriction = parseObjectRestriction(element, prefixes);
			val cardinality = (element \ "@cardinality").head.text.toInt;
			restriction match {
			case ObjectRestriction(property, Some(filler)) => factory.getOWLObjectExactCardinality(cardinality, property, filler);
			case ObjectRestriction(property, None) => factory.getOWLObjectExactCardinality(cardinality, property);
			}
	}

	def parseIndividual(element: Elem, prefixes: Map[String, String]): OWLIndividual = {
			element match {
			case <NamedIndividual/> => parseNamedIndividual(element, prefixes);
			case <AnonymousIndividual/> => parseAnonymousIndividual(element, prefixes);
			}
	}

	def parseNamedIndividual(element: Elem, prefixes: Map[String, String]): OWLNamedIndividual = {
			factory.getOWLNamedIndividual(parseIRI(element, prefixes));
	}

	def parseAnonymousIndividual(element: Elem, prefixes: Map[String, String]): OWLAnonymousIndividual = {
			val nodeID = (element \ "@nodeID").head.text;
			factory.getOWLAnonymousIndividual(nodeID);
	}

	def parsePropertyExpression(element: Elem, prefixes: Map[String, String]): OWLObjectPropertyExpression = {
			element match {
			case <ObjectProperty/> => parseObjectProperty(element, prefixes);
			case <ObjectInverseOf>{_*}</ObjectInverseOf> => parseInverseProperty(element, prefixes);
			}
	}

	def parseObjectProperty(element: Elem, prefixes: Map[String, String]): OWLObjectProperty = {
			factory.getOWLObjectProperty(parseIRI(element, prefixes));
	}

	def parseInverseProperty(element: Elem, prefixes: Map[String, String]): OWLObjectInverseOf = {
			val property = parseObjectProperty(children(element).head, prefixes);
			factory.getOWLObjectInverseOf(property);
	}

	def parseIRI(element: Elem, prefixes: Map[String, String]): IRI = {
			val iriString = (element \ "@IRI").headOption.map(_.text).getOrElse(expandAbbreviatedIRI((element \ "@abbreviatedIRI").head.text, prefixes));
			IRI.create(iriString);
	}

	def expandAbbreviatedIRI(value: String, prefixes: Map[String, String]): String = {
			val segments = value.split(":", 2);
			val localName = if (segments.size > 1) segments(1) else "";
			return prefixes(segments(0)) + localName;
	}

	def children(element: Elem): Seq[Elem] = {
			element.child.collect({case e: Elem => e}).toSeq;
	}

}