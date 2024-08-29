package org.clematis.skos.parser

import groovy.xml.XmlSlurper
import org.clematis.skos.parser.model.Concept
import org.clematis.skos.parser.model.IdAware
import org.clematis.skos.parser.model.LanguageString
import org.clematis.skos.parser.model.ObjectData
import org.clematis.skos.ResourceAnchor
import org.clematis.skos.parser.model.Taxonomy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RdfReader {

    static final Logger LOG = LoggerFactory.getLogger(RdfReader.class)

    static def getXmlReader(String fileName) {
        return getXmlReader(ResourceAnchor.getResourceAsStream(fileName))
    }

    static def getXmlReader(InputStream is) {

        XmlSlurper xmlSlurper = new XmlSlurper(false, true);
        xmlSlurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        return xmlSlurper
                .parse(is)
                .declareNamespace('rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
                        , 'km': 'https://ontology.clematis.org/Knowledge#'
                        , 'wbas': 'https://ontology.clematis.org/Base#'
                        , 'dct': 'http://purl.org/dc/terms'
                        , 'dc': 'http://purl.org/dc/elements/1.1'
                        , 'skos-xl': 'http://www.we.rog/2018/05/skos-xl#'
                        , 'skosxl': 'http://www.w3.org/2008/05/skos-xl#'
                        , 'skos': 'http://www.w3.org/2004/02/skos/core#'
                )
    }

    /**
     * Find a reference id which can be anything an author is willing to supply,
     * event text in the element itself
     *
     * @param element - broader element
     * @return identifier of a broader element as an author fancies.
     */
    static String findReferenceId(def element) {
        String uri = null

        if (!element.'@rdf:resource'.isEmpty()) {
            uri = element.'@rdf:resource'.text()

        } else if (!element.'@local:alpha'.isEmpty()) {
            uri = element.'@local:alpha'.text()

        } else if (!element.Concept.'@rdf:about'.isEmpty()) {
            uri = element.Concept.'@rdf:about'.text()

        } else if (!element.text().isEmpty()) {
            uri = element.text()

        }

        return uri
    }

    static void processNarrowerElements(xmlConcept, Taxonomy t) {

        Concept c = findConcept(xmlConcept, t)

        if (c == null) {
            return
        }

        for (narrower in xmlConcept.narrower) {

            final String narrowerId = findReferenceId(narrower)
            if (narrowerId) {

                Concept p = Taxonomy.findConceptById(narrowerId, t)

                if (p != null) {
                    p.setParent(c)
                } else {
                    LOG.error("No narrower concept with id=" + narrowerId)
                }
            }
        }
    }

    static void processBroaderElements(xmlConcept, Taxonomy t) {

        Concept c = findConcept(xmlConcept, t)

        if (c == null) {
            return
        }

        for (broader in xmlConcept.broader) {

            final String broaderId = findReferenceId(broader)
            if (broaderId) {
                Concept p = Taxonomy.findConceptById(broaderId, t)
                if (p != null) {
                    c.addParent(p)
                } else {
                    LOG.error("No parent with id=" + broaderId)
                }
            }
        }

        if (c.getParents().size() == 0) {
            t.addTopConcept(c)
        }
    }

    static void processRelatedElements(xmlConcept, Taxonomy t) {

        Concept c = findConcept(xmlConcept, t)

        if (c == null) {
            return
        }

        for (related in xmlConcept.related) {

            final String relatedId = findReferenceId(related)
            if (relatedId) {
                Concept p = Taxonomy.findConceptById(relatedId, t)
                if (p != null) {
                    c.addRelated(p)
                } else {
                    LOG.error("No related concept with id=" + relatedId)
                }
            }
        }
    }

    static Concept findConcept(xmlConcept, Taxonomy t) {

        String id = null
        Concept c = null

        if (xmlConcept.'@rdf:about'.text()) {
            id = ObjectData.validateUUID(xmlConcept.'@rdf:about'.text())
            c = t.getByUri(id)
        } else if (xmlConcept.'skos:prefLabel'.text()) {
            id = xmlConcept.'skos:prefLabel'.text()
            c = t.getByLabel(id)
        }

        if (c == null) {
            c = t.getByUri(UUIDHelper.getGeneratedIdsPool().get(id))
        }

        if (c == null) {
            LOG.error ("No concept found with id=" + id)
        }

        return c
    }

    static synchronized Taxonomy getTaxonomyFromXml(String fileName, Taxonomy reference = null) {
        return getTaxonomyFromXml(getXmlReader(fileName), reference)
    }

    static synchronized Taxonomy getTaxonomyFromXml(InputStream is, Taxonomy reference = null) {
        return getTaxonomyFromXml(getXmlReader(is), reference)
    }

    static String getExternalId(xmlConcept) {
        String uri = xmlConcept.'@rdf:about'.text()
        String uriLastPart = IdAware.getLastPart(uri)

        return xmlConcept.'dc:identifier'.isEmpty() ?
                (xmlConcept.'km:externalId'.isEmpty() ?
                        (uriLastPart ? uriLastPart : xmlConcept.'skos:prefLabel')
                        : xmlConcept.'km:externalId')
                : xmlConcept.'dc:identifier'
    }

    static String getId(xmlConcept, Taxonomy reference, List<LanguageString> prefLabels, String conceptExId) {

        String uri = xmlConcept.'@rdf:about'.text()

        if (!uri) {
            uri = conceptExId
        }

        if (reference != null) {
            Concept referenceConcept = reference.getByExtId(conceptExId)
            if (referenceConcept == null && prefLabels.size() > 0) {
                referenceConcept = reference.getByLabel(prefLabels.get(0).getValue())
            }
            if (referenceConcept != null) {
                UUIDHelper.getGeneratedIdsPool().put(uri, referenceConcept.getUri())
                uri = referenceConcept.getUri()
            } else {
                LOG.warn("Reference concept has not been found for: " + conceptExId)
            }
        }

        uri
    }

    static List<LanguageString> makePrefLabels(labelElements) {
        List<LanguageString> labels = new ArrayList<>()

        for (prefLabel in labelElements) {
            final String label = prefLabel.text()
            final String lang = prefLabel.'@xml:lang'
            labels.add(new LanguageString(label, lang))
        }

        labels
    }

    static List<LanguageString> makeLabels(labelElements, String conceptExId, List<LanguageString> prefLabels) {

        List<LanguageString> labels = new ArrayList<>()
        for (label in labelElements) {
            final String l = label.text()
            final String lang = label.'@xml:lang'
            def labelValue = new LanguageString(l, lang)
            if (!prefLabels.contains(labelValue) && !l.equals(conceptExId)) {
                labels.add(labelValue)
            } else if (prefLabels.contains(labelValue)) {
                LOG.warn("Skipped label {} as it is contained in pref label {}", labelValue, prefLabels)
            } else if (l.equals(conceptExId)) {
                LOG.warn("Skipped label {} as it is contained in external id {}", labelValue, conceptExId)
            }
        }

        labels
    }

    static synchronized Taxonomy getTaxonomyFromXml(rdf, Taxonomy reference = null) {

        Taxonomy t = null
        def xmlTaxonomy = rdf.ConceptScheme
        if (xmlTaxonomy.isEmpty()) {
            for (xmlConcept in rdf.Description) {
                final type = xmlConcept.type.'@rdf:resource'.text()
                if (type.contains("ConceptScheme")) {
                    xmlTaxonomy = xmlConcept
                    t = createTaxonomy(xmlTaxonomy)
                    break
                }
            }
        } else {
            t = createTaxonomy(xmlTaxonomy)
        }

        if (t == null) {
            LOG.error("Can't find a taxonomy element")
            return null
        }

        /*
         * Reference overrides id
         */
        if (reference != null) {
            t.setId(reference.getId())
        }

        for (xmlConcept in rdf.Concept) {
            Concept c = createConcept(xmlConcept, reference)
            t.addConcept(c)
        }

        for (xmlConcept in rdf.Concept) {
            processBroaderElements(xmlConcept, t)
            processNarrowerElements(xmlConcept, t)
            processRelatedElements(xmlConcept, t)
        }

        for (xmlConcept in rdf.Description) {
            final type = xmlConcept.type.'@rdf:resource'.text()
            if (type.equals("http://www.w3.org/2004/02/skos/core#Concept")) {
                Concept c = createConcept(xmlConcept, reference)
                t.addConcept(c)
            }
        }

        for (xmlConcept in rdf.Description) {
            final type = xmlConcept.type.'@rdf:resource'.text()
            if (type.equals("http://www.w3.org/2004/02/skos/core#Concept")) {
                processBroaderElements(xmlConcept, t)
                processNarrowerElements(xmlConcept, t)
                processRelatedElements(xmlConcept, t)
            }
        }

        return t
    }

    static synchronized Taxonomy createTaxonomy(xmlTaxonomy) {

        final String taxonomyUri = xmlTaxonomy.'@rdf:about'.text()
        final String doi = xmlTaxonomy.'km:doi'.text()

        final List<LanguageString> prefLabels = new ArrayList<>();
        for (int i = 0; i < xmlTaxonomy.'skos:prefLabel'.size(); i++) {
            prefLabels.add(new LanguageString(
                    xmlTaxonomy.'skos:prefLabel'[i].toString(),
                    xmlTaxonomy.'skos:prefLabel'[i].'@xml:lang'.toString())
            )
        }

        if (prefLabels.size() == 0 && !xmlTaxonomy.title.isEmpty()) {
            prefLabels.add(new LanguageString(xmlTaxonomy.title))
        }

        String tExternalId = xmlTaxonomy.identifier
        if (!tExternalId) {
            tExternalId = xmlTaxonomy.'externalId'
        }

        return new Taxonomy(taxonomyUri, tExternalId, prefLabels, doi)
    }

    static Concept createConcept(xmlConcept, Taxonomy reference) {

        final String conceptExId = getExternalId(xmlConcept)
        List<LanguageString> prefLabels = makePrefLabels(xmlConcept.'skos:prefLabel')
        List<LanguageString> altLabels = makeLabels(xmlConcept.'skos:altLabel', conceptExId, prefLabels)
        List<LanguageString> hiddenLabels = makeLabels(xmlConcept.'skos:hiddenLabel', conceptExId, prefLabels)
        List<LanguageString> definitions = makeLabels(xmlConcept.'skos:definition', conceptExId, prefLabels)
        String uri = getId(xmlConcept, reference, prefLabels, conceptExId)

        return new Concept(uri, conceptExId, prefLabels, altLabels, hiddenLabels, definitions)
    }

    static def validateIdsInRdf(String fileName, boolean regenerate = false) {

        def rdf = getXmlReader(fileName)
        def conceptSchemeId = null

        for (xmlConcept in rdf.Description) {
            final type = xmlConcept.type.'@rdf:resource'.text()

            final String uri = xmlConcept.'@rdf:about'.text()
            if (type.contains("ConceptScheme")) {
                conceptSchemeId = uri
                break
            }
        }

        for (xmlConcept in rdf.Description) {

            final type = xmlConcept.type.'@rdf:resource'.text()

            final String uri = xmlConcept.'@rdf:about'.text()
            if (type.contains("ConceptScheme")) {
                conceptSchemeId = uri
            }

            xmlConcept.'externalId'.replaceNode { }
            xmlConcept.'wbas:mnemonicId'.replaceNode { }
            xmlConcept.'@rdf:about' = ObjectData.validateUUID(regenerate ? uri.hashCode().toString() : uri )

            for (broader in xmlConcept.broader) {
                final String broaderId = broader.'@rdf:resource'.text()
                broader.'@rdf:resource' = ObjectData.validateUUID(regenerate ? broaderId.hashCode().toString() : uri)
            }

            if (!type.contains("ConceptScheme") && conceptSchemeId != null
                    && xmlConcept.inScheme.isEmpty()) {
                xmlConcept.appendNode {
                    'skos:inScheme'('rdf:resource': conceptSchemeId)
                }
            }

        }

        return rdf
    }

}
