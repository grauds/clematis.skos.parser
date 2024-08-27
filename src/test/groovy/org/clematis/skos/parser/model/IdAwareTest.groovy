package org.clematis.skos.parser.model

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotEquals
import org.clematis.skos.parser.UUIDHelper
import org.junit.jupiter.api.Test


class IdAwareTest {

    @Test
    void handleEmptyUri() {
        def uri = ""
        def newUri = IdAware.validateUUID(uri)
        assertNotEquals(uri, IdAware.getLastPart(newUri))
    }

    @Test
    void handleUUIDUri() {
        def uri = UUID.randomUUID()
        def newUri = IdAware.validateUUID(uri.toString())
        assertNotEquals(uri, IdAware.getLastPart(newUri))
    }

    @Test
    void handleBasicUri() {
        def uri = "My_ID"
        def newUri = IdAware.validateUUID(uri)
        assertNotEquals(uri, IdAware.getLastPart(newUri))
        assertEquals(newUri, UUIDHelper.title2UUID(uri))
    }

    @Test
    void testFormat() {
        assertEquals(IdAware.getURI_FMT_PREFIX() + "/54", IdAware.formatToURI("54"))
        assertEquals(IdAware.getURI_FMT_PREFIX() + "/54", IdAware.formatToURI(IdAware.getURI_FMT_PREFIX() + "/54"))
    }
}
