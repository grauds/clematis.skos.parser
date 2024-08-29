package org.clematis.skos.parser.model

import org.clematis.skos.parser.TaxonomyUtils

import java.util.concurrent.ConcurrentHashMap

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.clematis.skos.parser.UUIDHelper


class Taxonomy extends ObjectData {

    private static final Logger LOG = LoggerFactory.getLogger(Taxonomy.class)

    private String doi

    private final Set<Concept> topConcepts = ConcurrentHashMap.newKeySet()

    private final Map<String, Concept> extId2concepts = new ConcurrentHashMap<>()
    private final Map<String, Concept> uri2concept = new ConcurrentHashMap<>()
    private final Map<String, Concept> label2concept = new ConcurrentHashMap<>()

    Taxonomy() {
        super()
    }

    Taxonomy(String uri) {
        super(uri)
    }

    Taxonomy(String externalId, List<LanguageString> prefLabel) {
        super(externalId, prefLabel)
    }

    Taxonomy(String uri, String externalId, List<LanguageString> prefLabel, String doi) {
        super(uri, externalId, prefLabel)
        this.doi = doi
    }

    Taxonomy(Taxonomy t) {
        this(
                (String) formatToURI(t.id),
                (String) t.externalId,
                (List<LanguageString>) t.prefLabel,
                (String) t.doi
        )
    }

    static Concept findConceptById(String id, Taxonomy t) {

        // check if the broader id is an UUID and construct URI for it
        final String uri = formatToURI(id)
        Concept p = t.getByUri(uri)

        // falling back to search by the former ID
        if (p == null) {
            p = t.getByUri(UUIDHelper.getGeneratedIdsPool().get(id))
        }

        // try to search in taxonomy as it was a label
        if (p == null) {
            p = t.getByLabel(id)
        }

        // try to search in taxonomy as it was an external id
        if (p == null) {
            p = t.getByExtId(id)
        }

        return p
    }

    Concept getByExtId(String l) {
        return l != null ? extId2concepts.get(l) : null
    }

    Concept getByLabel(String l) {
        return l != null ? label2concept.get(l) : null
    }

    Concept getByUri(String l) {
        return l != null ? uri2concept.get(l) : null
    }

    void addTopConcept(Concept c) {
        if (c == null) {
            new RuntimeException("Null concept is being added")
        }

        topConcepts.add(c)
    }

    void addConcept(Concept c) {

        if (c == null) {
            new RuntimeException("Null concept is being added")
        }

        if (uri2concept.put(formatToURI(c.id), c) != null) {
            new RuntimeException("Duplicate concept by URI: " + formatToURI(c.id))
        }

        if (c.externalId && !c.externalId.isEmpty()) {
            Concept prev = extId2concepts.put(c.externalId.trim(), c)
            if (prev != null) {
                LOG.warn("Duplicate external ID: " + c.externalId.trim())
            }
        }

        if (!c.prefLabel.isEmpty()) {
            Concept prev = label2concept.put(c.prefLabel.get(0).getValue().trim(), c)
            if (prev != null) {
                LOG.warn("Duplicate label: " + c.prefLabel.get(0).getValue().trim())
            }
        }
    }

    String getDoi() {
        return doi
    }

    Set<Concept> getTopConcepts() {
        return topConcepts
    }

    Collection<Concept> getConcepts() {
        return uri2concept.values()
    }


    int getSize() {
        return this.uri2concept.size()
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false

        Taxonomy taxonomy = (Taxonomy) o

        if (doi != taxonomy.doi) {
            LOG.error("Taxonomy DOIs are different: {} and {}", doi, taxonomy.doi)
            return false
        }
        if (!TaxonomyUtils.equal(extId2concepts, taxonomy.extId2concepts)) {
            LOG.error("External id/concepts maps are different: {} and {}", extId2concepts, taxonomy.extId2concepts)
            return false
        }
        if (!TaxonomyUtils.equal(topConcepts, taxonomy.topConcepts)) {
            LOG.error("Top concepts are different: {} and {}", topConcepts, taxonomy.topConcepts)
            return false
        }
        if (!TaxonomyUtils.equal(uri2concept, taxonomy.uri2concept)) {
            LOG.error("External id maps are different: {} and {}", uri2concept, taxonomy.uri2concept)
            return false
        }

        return true
    }

    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + doi.hashCode()
        result = 31 * result + extId2concepts.hashCode()
        result = 31 * result + uri2concept.hashCode()
        result = 31 * result + topConcepts.hashCode()
        return result
    }
}
