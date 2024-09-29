package org.clematis.skos.parser

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

import org.clematis.skos.ResourceAnchor
import org.clematis.skos.parser.handlers.DescriptionHandler
import org.clematis.skos.parser.model.Concept
import org.clematis.skos.parser.model.ObjectData
import org.clematis.skos.parser.model.Taxonomy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler


class RdfStreamReader extends DefaultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RdfStreamReader.class)

    private final List<IReaderListener> listeners = new ArrayList<>();

    private DescriptionHandler descriptionHandler = new DescriptionHandler()

    private Taxonomy t

    @Override
    void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        descriptionHandler.startElement(uri, localName, qName, attributes)
    }

    @Override
    void characters(char[] ch, int start, int length) throws SAXException {
        descriptionHandler.characters(ch, start, length)
    }

    @Override
    void endElement(String uri, String localName, String qName) throws SAXException {
        descriptionHandler.endElement(uri, localName, qName)
        ObjectData o = descriptionHandler.getObject()
        if (o instanceof Concept) {
            t.addConcept((Concept) o)
            fireConceptCreated((Concept) o)
        } else if (o instanceof Taxonomy) {
            t = (Taxonomy) o
            fireTaxonomyCreated(t)
        }
    }

    void addReaderListener(IReaderListener readerListener) {
        if (readerListener != null) {
            this.listeners.add(readerListener)
        }
    }

    void fireConceptCreated(Concept c) {
        for (IReaderListener listener : this.listeners) {
            listener.conceptCreated(c)
        }
    }

    void fireTaxonomyCreated(Taxonomy t) {
        for (IReaderListener listener : this.listeners) {
            listener.taxonomyCreated(t)
        }
    }

    void fireParentAdded(Concept c, Concept p) {
        for (IReaderListener listener : this.listeners) {
            listener.parentAdded(c, p)
        }
    }

    static synchronized Taxonomy getTaxonomyFromXml(String fileName, Taxonomy reference = null) {
        return getTaxonomyFromXml(ResourceAnchor.getResourceAsStream(fileName), reference)
    }

    static synchronized Taxonomy getTaxonomyFromXml(InputStream is, Taxonomy reference = null) {

        SAXParserFactory factory = SAXParserFactory.newInstance()
        SAXParser parser = factory.newSAXParser()
        RdfStreamReader reader = new RdfStreamReader()

        parser.parse(is, reader)

        for (Concept c : reader.t.getConcepts()) {
            processBroaderElements(c, reader.t)
        }

        return reader.t
    }

    static void processBroaderElements(Concept c, Taxonomy t) {

        Set<Concept> parents = new LinkedHashSet<>()
        parents.addAll(c.getParents())
        c.getParents().clear()

        for (Concept b in parents) {
            Concept p = Taxonomy.findConceptById(b.getId(), t)
            if (p != null) {
                c.addParent(p)
                fireParentAdded(c, p)
            } else {
                LOG.error("No parent with id=" + b.getId())
            }
        }

        if (c.getParents().size() == 0) {
            t.addTopConcept(c)
        }
    }
}

