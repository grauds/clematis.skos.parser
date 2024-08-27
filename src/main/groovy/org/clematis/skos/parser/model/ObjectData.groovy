package org.clematis.skos.parser.model

import java.nio.charset.StandardCharsets

import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.clematis.skos.parser.UUIDHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ObjectData extends IdAware {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectData.class)

    static final String ENCODING = StandardCharsets.UTF_8.displayName()

    List<LanguageString> prefLabel = new ArrayList<>()

    ObjectData() {
        super()
    }

    ObjectData(String uri) {
        this.setUri(uri)
    }

    ObjectData(String externalId, List<LanguageString> prefLabel) {
        this()
        this.prefLabel = prefLabel
        this.externalId = externalId
    }

    ObjectData(String uri, String externalId, List<LanguageString> prefLabel) {
        this(uri)
        this.prefLabel = prefLabel
        this.externalId = externalId != null ? externalId.trim() : externalId
    }

    static String findKey(String value) {
        return UUIDHelper
                .generatedIdsPool
                .entrySet()
                .stream()
                .filter({ c -> c.getValue().equals(ObjectData.format(value)) })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null)
    }


    void addPrefLabel(String label) {
        if (label != null) {
            this.prefLabel.add(new LanguageString(label))
        }
    }

    void addPrefLabel(LanguageString label) {
        if (label != null) {
            this.prefLabel.add(label)
        }
    }

    static String unescapeHtml(String value) {
        final String result = StringEscapeUtils.unescapeHtml3(value)
        return result.replaceAll("&hyphen;", "\u2010")
    }

    static String escapeHtml(String value) {
        final String result = StringEscapeUtils.unescapeJava(value)
        return result.replaceAll("\u2010", "&hyphen;")
    }

    static String trimQuotes(String value) {
        return StringUtils.strip(value, '" ')
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (!super.equals(o)) return false

        ObjectData that = (ObjectData) o

        if (!prefLabel.equals(that.prefLabel)) {
            LOG.error("Pref labels for {} and {} are different: {} and {}", this.id, that.id, prefLabel, that.prefLabel)
            return false
        }

        return true
    }

    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + (prefLabel != null ? prefLabel.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return "{prefLabel=" + prefLabel + ", " + super.toString() + "}"
    }
}
