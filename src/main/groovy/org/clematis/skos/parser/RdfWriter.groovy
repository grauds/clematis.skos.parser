package org.clematis.skos.parser

import org.clematis.skos.parser.model.LanguageString

import java.nio.file.Path

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.clematis.skos.parser.model.ObjectData
import org.clematis.skos.parser.model.Taxonomy


class RdfWriter {

    static void saveTaxonomyToFile(Path inFile, Taxonomy t) {

        def builder = new StreamingMarkupBuilder()
        builder.encoding = ObjectData.ENCODING
        final String taxonomyUri = t.getUri()

        def topConceptOfClosure = { parent->
            topConceptOf('rdf:resource': parent.getUri())
        }
        final def broaderClosure = { parent->
            broader('rdf:resource': parent.getUri())
        }

        def saveClosure
        saveClosure = { parent, chidren, parentClosure, stack ->
            parentClosure.delegate = delegate
            LOG.info("Writing children of: " + UUIDHelper.findKey(parent.id))

            try {
                stack.push(parent)
                for (concept in chidren) {
                    LOG.info("Writing child " +  UUIDHelper.findKey(concept.id))
                    Concept('rdf:about': concept.getUri()) {
                        concept.prefLabel.each { l->
                            if (l instanceof LanguageString && l.getLanguage() != null && !l.getLanguage().equals("")) {
                                'prefLabel' 'xml:lang': l.getLanguage(), l.getValue()
                            } else if (l instanceof LanguageString) {
                                'prefLabel' l.getValue()
                            } else {
                                'prefLabel' l
                            }
                        }
                        if (concept.externalId) {
                            'km:externalId' concept.externalId
                        }
                        concept.altLabel.each { l->
                            if (l.getLanguage() != null && !l.getLanguage().equals("")) {
                                'altLabel' 'xml:lang': l.getLanguage(), l.getValue()
                            } else {
                                'altLabel' l.getValue()
                            }
                        }
                        concept.hiddenLabel.each { l->
                            if (l.getLanguage() != null && !l.getLanguage().equals("")) {
                                'hiddenLabel' 'xml:lang': l.getLanguage(), l.getValue()
                            } else {
                                'hiddenLabel' l.getValue()
                            }
                        }
                        concept.definition.each { l->
                            if (l.getLanguage() != null && !l.getLanguage().equals("")) {
                                'definition' 'xml:lang': l.getLanguage(), l.getValue()
                            } else {
                                'definition' l.getValue()
                            }
                        }

                        parentClosure(parent)
                        if (parentClosure != topConceptOfClosure) {
                            inScheme('rdf:resource': t.getUri())
                        }
                        for (child in concept.children) {
                            narrower('rdf:resource': child.getUri())
                        }
                        for (r in concept.related) {
                            related('rdf:resource': r.getUri())
                        }
                    }
                }
                for (concept in chidren) {
                    saveClosure(concept, concept.children, broaderClosure, stack)
                }

                LOG.info("Written children of: " + UUIDHelper.findKey(parent.id))
                stack.pop()

            } catch(StackOverflowError er) {
                throw er
            }
        }

        def taxonomy = builder.bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace('': 'http://www.w3.org/2004/02/skos/core#')
            mkp.declareNamespace(rdf: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
            mkp.declareNamespace(wbas: 'https://ontology.clematis.org/Base#')
            mkp.declareNamespace('km': 'https://ontology.clematis.org/Knowledge#')
            'rdf:RDF' {
                ConceptScheme('rdf:about': taxonomyUri) {
                    t.prefLabel.each { l->
                        'prefLabel' l
                    }
                    if (t.externalId) {
                        'km:externalId' t.externalId
                    }
                    if (t.doi) {
                        'km:doi' t.doi
                    }
                    for (concept in t.topConcepts) {
                        hasTopConcept('rdf:resource': concept.getUri())
                    }
                }
                saveClosure.delegate = delegate
                saveClosure(t, t.topConcepts, topConceptOfClosure, new Stack<String>())
            }
        }

        final String fileName = "new-" + t.prefLabel + "-taxonomy.xml"
        final File f = inFile.resolve(fileName).toFile()
        f.delete()
        f.parentFile.mkdirs()

        f.withWriter(ObjectData.ENCODING) { writer ->
            writer.write(taxonomy.toString())
        }
    }

    static void saveTaxonomyRdfToFile(Path inFile, String fileName, def rdf) {

        def builder = new StreamingMarkupBuilder()
        builder.encoding = ObjectData.ENCODING

        final File f = inFile.resolve(fileName).toFile()
        f.delete()
        f.parentFile.mkdirs()

        def taxonomy = builder.bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace('skos': 'http://www.w3.org/2004/02/skos/core#')
            mkp.declareNamespace('clematis-model': 'http://model.clematis.org/ontologies/EntityStore.owl#')
            mkp.declareNamespace('luxids': 'http://www.temis.com/luxid-schema#')
            mkp.declareNamespace('rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
            mkp.declareNamespace('rdfs': 'http://www.w3.org/2000/01/rdf-schema#')
            mkp.declareNamespace('wbas': 'https://ontology.clematis.org/Base#')
            mkp.declareNamespace('km': 'https://ontology.clematis.org/Knowledge#')
            mkp.yield rdf
        }

        f.withWriter(ObjectData.ENCODING) { writer ->
            XmlUtil.serialize( taxonomy.toString(), writer )
        }

    }

}
