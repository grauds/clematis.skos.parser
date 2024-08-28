package org.clematis.skos.parser.handlers

import org.clematis.skos.parser.RdfHandler
import org.xml.sax.Attributes
import org.xml.sax.SAXException

/**
 * <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#ConceptScheme"/>
 */
class RdfTypeHandler extends RdfHandler<String> {

    private String type

    @Override
    void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (getTag() == qName) {
            type = attributes.getValue("rdf:resource")
        }
    }

    @Override
    void endElement(String uri, String localName, String qName) throws SAXException {

    }

    @Override
    void clear() {
        type = ""
    }

    @Override
    String getObject() {
        return type
    }

    @Override
    String getTag() {
        return "rdf:type"
    }
}
