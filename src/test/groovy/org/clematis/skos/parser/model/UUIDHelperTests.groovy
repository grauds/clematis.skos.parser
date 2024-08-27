package org.clematis.skos.parser.model

import static junit.framework.TestCase.assertEquals
import static junit.framework.TestCase.assertTrue
import org.clematis.skos.parser.UUIDHelper
import org.junit.jupiter.api.Test

class UUIDHelperTests {

    @Test
    void testUriGeneration() {

        Concept concept = new Concept("A")
        assertTrue(concept.getUri().startsWith("https://data.clematis.org"))
        assertTrue(UUIDHelper.generatedIdsPool.get("A").equals(concept.getUri()))

        String wrongUri = "http://data.clematis.org/QQQ"
        concept.setUri(wrongUri)
        assertTrue(UUIDHelper.generatedIdsPool.get(wrongUri).equals(concept.getUri()))
    }

    @Test
    void testUriGenerationDoubles() {

        String incorrectLongUri = "https://webarchive.nationalarchives.gov.uk/20130313174658/http://www.education.gov.uk/vocabularies/educationtermsandtags/550"
        Concept concept = new Concept(incorrectLongUri)
        assertTrue(concept.getUri().startsWith("https://data.clematis.org"))
        assertTrue(UUIDHelper.generatedIdsPool.get(incorrectLongUri).equals(concept.getUri()))
    }

    @Test
    void testLabelSubstitution() {
        String rawTitle = "SOME_TITLE"
        Concept anotherConcept = new Concept(rawTitle)
        anotherConcept.addPrefLabel(rawTitle)

        assertEquals(rawTitle, anotherConcept.getPrefLabel().get(0).getValue())
    }
}
