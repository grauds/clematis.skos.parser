package org.clematis.skos.parser.handlers

import org.clematis.skos.parser.RdfHandler
import org.clematis.skos.parser.model.Concept
import org.xml.sax.Attributes
import org.xml.sax.SAXException

class SkosBroaderHandler extends RdfHandler<List<Concept>> {

    List<Concept> broaders = new ArrayList<>()

    @Override
    void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (getTag() == qName) {
            broaders.add(new Concept(attributes.getValue("rdf:resource")))
        }
    }

    @Override
    void clear() {
        broaders = new ArrayList<>()
    }

    @Override
    List<Concept> getObject() {
        return broaders
    }

    @Override
    String getTag() {
        return "skos:broader"
    }
}
