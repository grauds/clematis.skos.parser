package org.clematis.skos.parser.handlers

import org.clematis.skos.parser.RdfHandler
import org.clematis.skos.parser.model.Concept
import org.clematis.skos.parser.model.LanguageString
import org.clematis.skos.parser.model.ObjectData
import org.clematis.skos.parser.model.Taxonomy
import org.xml.sax.Attributes
import org.xml.sax.SAXException

class DescriptionHandler extends RdfHandler<ObjectData> {

    public static final String RDF_ABOUT_TAG = "rdf:about"
    public static final String CONCEPT_SCHEME_TYPE = "http://www.w3.org/2004/02/skos/core#ConceptScheme"
    public static final String CONCEPT_TYPE = "http://www.w3.org/2004/02/skos/core#Concept"

    private RdfTypeHandler typeHandler = new RdfTypeHandler()
    private SkosPrefLabelHandler skosPrefLabelHandler = new SkosPrefLabelHandler()
    private SkosBroaderHandler skosBroaderHandler = new SkosBroaderHandler()

    private ObjectData object

    @Override
    void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes)

        if (getTag() == qName) {
            clear() // clear the context once the new element is started
            currentObject.put(RDF_ABOUT_TAG, attributes.getValue(RDF_ABOUT_TAG))
        } else if (open) {
            typeHandler.startElement(uri, localName, qName, attributes)
            skosPrefLabelHandler.startElement(uri, localName, qName, attributes)
            skosBroaderHandler.startElement(uri, localName, qName, attributes)
        }
    }

    @Override
    void characters(char[] ch, int start, int length) throws SAXException {
        if (open) {
            skosPrefLabelHandler.characters(ch, start, length)
        }
    }

    void clear() {
        object = null
        currentObject.clear()

        skosPrefLabelHandler.clear()
        skosBroaderHandler.clear()
        typeHandler.clear()
    }

    @Override
    void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName)

        if (getTag() == qName) {
            String type = typeHandler.getObject()

            if (CONCEPT_SCHEME_TYPE == type) {
                object = createTaxonomy()
            } else if (CONCEPT_TYPE == type) {
                object = createConcept()
            }

        } else if (open) {
            typeHandler.endElement(uri, localName, qName)
            skosPrefLabelHandler.endElement(uri, localName, qName)
            skosBroaderHandler.endElement(uri, localName, qName)
        }
    }

    @Override
    ObjectData getObject() {
        return object
    }

    @Override
    String getTag() {
        return "rdf:Description"
    }

    Taxonomy createTaxonomy() {
        final String taxonomyUri = currentObject.get("rdf:about")
        return new Taxonomy(taxonomyUri)
    }

    Concept createConcept() {
        final String conceptUri = currentObject.get("rdf:about")
        Concept c = new Concept(conceptUri)
        c.addPrefLabel(new LanguageString(skosPrefLabelHandler.getObject()))
        List<Concept> broaders = skosBroaderHandler.getObject()
        for (Concept b : broaders) {
            c.addParent(b)
        }
        return c
    }
}
