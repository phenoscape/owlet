owlet
==========

**owlet** is a query expansion preprocessor for SPARQL. It parses embedded OWL class expressions and uses an OWL reasoner to replace them with FILTER statements containing the URIs of subclasses of the given class expression (or superclasses, equivalent classes, or instances)

**owlet** is written in Scala but can be used in any Java application.
