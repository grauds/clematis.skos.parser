package org.clematis.skos.parser.handlers

import org.clematis.skos.parser.RdfHandler
import org.xml.sax.SAXException

class SkosPrefLabelHandler extends RdfHandler<String> {

    private String prefLabel

    @Override
    void characters(char[] ch, int start, int length) throws SAXException {
        if (open) {
            prefLabel = new String(Arrays.copyOfRange(ch, start, start + length))
        }
    }

    @Override
    void clear() {
        prefLabel = ""
    }

    @Override
    String getObject() {
        return prefLabel
    }

    @Override
    String getTag() {
        return "skos:prefLabel"
    }
}
