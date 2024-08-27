package org.clematis.skos.parser.model

import org.clematis.skos.parser.UUIDHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IdAware {

    private static final Logger LOG = LoggerFactory.getLogger(IdAware.class)

    static final String URI_FMT = "https://data.clematis.org/%s"
    static final String URI_FMT_PREFIX = "https://data.clematis.org"

    String id
    String externalId
    String uri

    IdAware() {
        if (id == null) {
            id = UUID.randomUUID().toString()
        }
        this.uri = formatToURI(this.id)
    }

    static String formatToURI(String id) {
        if (id != null && !id.startsWith(URI_FMT_PREFIX)) {
            return String.format(URI_FMT, id)
        } else {
            return id
        }
    }

    void setId(String id) {
        this.id = id
        this.uri = formatToURI(this.id)
    }

    void setUri(String uri) {
        this.uri = validateUUID(uri)
        this.id  = getLastPart(this.uri)
    }

    static String getLastPart(String value) {

        if (value != null) {
            final int pos = value.lastIndexOf('/')
            if (pos >= 0) {
                return value.substring(pos + 1, value.length())
            }
        }
        return value
    }

    static String validateUUID(String id) {
        /*
         * http://domain.com/mystuff/98822 -> 98822 or null
         */
        String last = getLastPart(id)
        /*
         * Check if it is already a UUID
         */
        if (!isUUID(last)) {

            def key = id

            if (!UUIDHelper.generatedIdsPool.containsKey(key)) {
                String uuid = UUID.randomUUID().toString()
                LOG.info("Generated " + uuid + " for " + key)
                UUIDHelper.generatedIdsPool.put(key, formatToURI(uuid))
                id = formatToURI(uuid)
            } else {
                id = UUIDHelper.generatedIdsPool.get(key)
                LOG.info("Got " + id + " for " + key)
            }
        }
        /*
         * Always return a UUID
         */
        return id
    }

    static boolean isUUID(String id) {
        try {
            return id != null && UUID.fromString(id)
        } catch (IllegalArgumentException ignored) {
            return false
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        IdAware that = (IdAware) o

        if (id != that.id) {
            LOG.error("Ids for {} and {}", this.id, that.id)
            return false
        }
        if (uri != that.uri) {
            LOG.error("Uris for {} and {}", this.id, that.id, uri, that.uri)
            return false
        }
        if (externalId != that.externalId) {
            LOG.error("Uris for {} and {}", this.id, that.id, externalId, that.externalId)
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (externalId != null ? externalId.hashCode() : 0)
        result = 31 * result + (uri != null ? uri.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return "{id='" + id + '\', externalId=' + externalId + '\'' + ", uri='" + uri + '\'}';
    }
}
