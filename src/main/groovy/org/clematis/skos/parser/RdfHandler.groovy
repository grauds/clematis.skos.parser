package org.clematis.skos.parser

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

abstract class RdfHandler<T> extends DefaultHandler {

    boolean open = false

    Map<String, Object> currentObject = new HashMap<>()

    @Override
    void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (getTag() == qName) {
            open = true
        }
    }

    @Override
    void endElement(String uri, String localName, String qName) throws SAXException {
        if (getTag() == qName) {
            open = false
        }
    }

    abstract void clear();

    abstract T getObject();

    abstract String getTag();
}