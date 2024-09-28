# Clematis SKOS Parsers

[![Gradle Package](https://github.com/grauds/clematis.skos.parser/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/grauds/clematis.skos.parser/actions/workflows/gradle-publish.yml)


This library provides parsers for [SKOS](https://www.w3.org/2004/02/skos/) files in two formats:

1. Described by RDF tags with SKOS attributes, for instance:

```xml
<rdf:RDF 
         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:skos="http://www.w3.org/2004/02/skos/core#">
    
    <rdf:Description rdf:about="https://domain.com/id">
        <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#ConceptScheme"/>
        <skos:prefLabel xml:lang="en">My Concept Scheme</skos:prefLabel>
    </rdf:Description>
    
    <rdf:Description rdf:about="https://domain.com/id2">
	<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
        <skos:prefLabel xml:lang="en">My Concept</skos:prefLabel>
        <skos:altLabel xml:lang="en">My Alt Concept Name</skos:altLabel>
        <skos:inScheme rdf:resource="https://domain.com/id"/>
    </rdf:Description>
</rdf:RDF>
```
2. Described with SKOS tags in default namespace plus RDF tags in a separate namespace, for instance:

```xml
<rdf:RDF 
         xmlns="http://www.w3.org/2004/02/skos/core#"
         xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    
    <ConceptScheme rdf:about="https://domain.com/id">
        <prefLabel xml:lang="en">My Concept Scheme</prefLabel>
    </ConceptScheme>
    
    <Concept rdf:about="https://domain.com/id2">
        <prefLabel xml:lang="en">My Concept</prefLabel>
        <altLabel xml:lang="en">My Alt Concept Name</altLabel>
        <inScheme rdf:resource="https://domain.com/id"/>
    </Concept>
</rdf:RDF>
```

## Quick Start

### RdfReader

First parser is based on [Groovy Slurper for XML ](https://groovy-lang.org/processing-xml.html#_xmlparser_and_xmlslurper) and allows modification of the SKOS source XML. It supports both formats, mentioned above. The only pitfall is even though Groovy XMLSlurper has a small memory footprint, it reads the whole DOM into the memory and if the file it big enough, it may be impossible to parse it.

To use it, simply call the parsing method with the file name or input stream:
```groovy
Taxonomy taxonomy = RdfReader.getTaxonomyFromXml("parser/rdf_sample.xml")
```
It will give back a Taxonomy class instance which is readonly in terms of XML processing. Also, it is possible to manipulate XML according to [Groovy docs](https://groovy-lang.org/processing-xml.html#_manipulating_xml).
```groovy
def rdf = RdfReader.getXmlReader("parser/rdf_sample.xml")
rdf.Description[1].replaceNode {
    Description(id: "https://domain.com/id2") {
        prefLabel("To Kill a Mockingbird")
    }
}
assert rdf.Description[1].prefLabel.text() == "To Kill a Mockingbird"    
```

### RdfStreamReader

Another parser is for the big files and it only supports RDF notation with attributes in SKOS namespace (option 1) so far. It is built on Java Sax parser and provides a collection of classes extending ```org.xml.sax.helpers.DefaultHandler```. To use it, simply call the parsing method with the file name or input stream:

```groovy
Taxonomy taxonomy = RdfStreamReader.getTaxonomyFromXml("parser/rdf_sample.xml")
```
It will also give back a Taxonomy class instance which is readonly in terms of XML processing.

## Circular dependencies detection

It is possible to detect circular dependencies after SKOS file is parsed, i.e. algorithm is working with Groovy objects in memory:

```groovy
Taxonomy taxonomy = RdfStreamReader.getTaxonomyFromXml("parser/rdf_sample.xml")
List<Stack<Concept>> paths = TaxonomyUtils.getCircularDependencies(taxonomy)
```

The method will return all the paths as a list of stacked concepts.
