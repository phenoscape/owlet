owlet
==========

**owlet** is a query expansion preprocessor for SPARQL. It parses embedded OWL class expressions and uses an OWL reasoner to replace them with FILTER statements containing the URIs of subclasses of the given class expression (or superclasses, equivalent classes, or instances).

**owlet** is written in Scala but can be used in any Java application.

## Installation
owlet is not yet available from an online Maven repository. To use it in your project you will need to:

- Check out the code, e.g. `git clone https://github.com/phenoscape/owlet.git`
- Run `mvn install` to build the jar and add to your local Maven repository.
- Add the owlet dependency to your `pom.xml`: 

```xml
<dependency>
			<groupId>org.phenoscape</groupId>
			<artifactId>owlet</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
```

## Usage

More documentation can be found on the [owlet wiki](https://github.com/phenoscape/owlet/wiki).
