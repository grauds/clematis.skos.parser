package org.clematis.skos.parser.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory


class Concept extends ObjectData {

    private static final Logger LOG = LoggerFactory.getLogger(Concept.class)

    private List<LanguageString> altLabel = new ArrayList<>()

    private List<LanguageString> hiddenLabel = new ArrayList<>()

    private Set<Concept> parents = new LinkedHashSet<>()

    private Set<Concept> children = new LinkedHashSet<>()

    private Set<Concept> related = new LinkedHashSet<>()

    List<LanguageString> definition = new ArrayList<>()

    Concept(String uri) {
        super(uri)
    }

    Concept(String uri,
            String externalId,
            List<LanguageString> prefLabel) {

        super(uri, externalId, prefLabel)
    }

    Concept(String uri,
            String externalId,
            List<LanguageString> prefLabel,
            List<LanguageString> altLabel) {

        this(uri, externalId, prefLabel)
        this.altLabel = altLabel
    }

    Concept(String uri, String externalId,
            List<LanguageString> prefLabel,
            List<LanguageString> altLabel,
            List<LanguageString> hiddenLabel) {

        this(uri, externalId, prefLabel, altLabel)
        this.hiddenLabel = hiddenLabel
    }

    Concept(String uri,
            String externalId,
            List<LanguageString> prefLabel,
            List<LanguageString> altLabel,
            List<LanguageString> hiddenLabel,
            List<LanguageString> definition) {

        this(uri, externalId, prefLabel, altLabel, hiddenLabel)
        this.definition = definition
    }

    Concept(String uri,
            String externalId,
            List<LanguageString> prefLabel,
            List<LanguageString> altLabel,
            Concept parent) {

        this(uri, externalId, prefLabel, altLabel)
        addParent(parent)
    }

    Concept(String uri,
            String externalId,
            List<LanguageString> prefLabel,
            List<LanguageString> altLabel,
            List<LanguageString> hiddenLabel,
            Concept parent) {

        this(uri, externalId, prefLabel, altLabel, hiddenLabel)
        addParent(parent)
    }

    void addAltLabel(LanguageString label) {
        if (!prefLabel.contains(label) && label.getValue() != externalId) {
            this.altLabel.add(label)
        } else if (prefLabel.contains(label)) {
            LOG.warn("Skipped alt label {} as it is contained in pref label {}", label, prefLabel)
        } else if (label.getValue().equals(externalId)) {
            LOG.warn("Skipped alt label {} as it is contained in external id {}", label, externalId)
        }
    }

    void addDefinition(LanguageString definition) {
        this.definition.add(definition)
    }

    void addHiddenLabel(LanguageString label) {
        if (!prefLabel.contains(label) && label.getValue() != externalId) {
            this.hiddenLabel.add(label)
        } else if (prefLabel.contains(label)) {
            LOG.warn("Skipped hidden label {} as it is contained in pref label {}", label, prefLabel)
        } else if (label.getValue() == externalId) {
            LOG.warn("Skipped hidden label {} as it is contained in external id {}", label, externalId)
        }
    }

    void addParent(Concept parent) {
        if (parent != null) {
            this.parents.add(parent)
            parent.addChild(this)
        }
    }

    void addChild(Concept child) {
        if (child != null) {
            this.children.add(child)
        }
    }

    void addRelated(Concept concept) {
        if (concept != null) {
            this.related.add(concept)
        }
    }

    void setParent(Concept parent) {
        if (parent != null) {
            this.parents.clear()
            addParent(parent)
        }
    }

    Collection<Concept> getParents() {
        return Collections.unmodifiableSet(this.parents)
    }

    Collection<Concept> getRelated() {
        return Collections.unmodifiableSet(this.related)
    }

    Collection<Concept> getChildren() {
        return Collections.unmodifiableSet(this.children)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false

        Concept concept = (Concept) o

        if (!altLabel.equals(concept.altLabel)) {
            LOG.error("Alt labels for {} and {}: {} and {}", this.id, concept.id, altLabel, concept.altLabel)
            return false
        }
        if (!definition.equals(concept.definition)) {
            LOG.error("Definitions for {} and {}: {} and {}", this.id, concept.id, definition, concept.definition)
            return false
        }
        if (!hiddenLabel.equals(concept.hiddenLabel)) {
            LOG.error("Hidden labels for {} and {}: {} and {}", this.id, concept.id, hiddenLabel, concept.hiddenLabel)
            return false
        }
      //  if (!parents.size().equals(concept.parents.size())) return false
      //  if (!related.size().equals(concept.related.size())) return false
       // if (!children.equals(concept.children)) return false

        return true
    }

    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + altLabel.hashCode()
        result = 31 * result + hiddenLabel.hashCode()
        result = 31 * result + definition.hashCode()
        return result
    }


    @Override
    String toString() {
        return "{" + super.toString() + "}"
    }
}
