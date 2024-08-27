package org.clematis.skos.parser

import org.clematis.skos.parser.model.Concept
import org.clematis.skos.parser.model.LanguageString
import org.clematis.skos.parser.model.Taxonomy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class RdfReaderTest {

    @BeforeEach
    void before() {
        UUIDHelper.generatedIdsPool.clear()
    }

    @Test
    void testBasicRdf() {
        Taxonomy taxonomy = RdfReader.getTaxonomyFromXml("parser/rdf_sample.xml")

        Assertions.assertEquals(1, taxonomy.getTopConcepts().size())

        Concept c = taxonomy.getTopConcepts()[0]
        Assertions.assertNotNull(c.prefLabel)
        Assertions.assertTrue(c.prefLabel instanceof List)
        Assertions.assertTrue(c.prefLabel.get(0) instanceof LanguageString)
        Assertions.assertEquals("Physiology", c.prefLabel.get(0).getValue())
        Assertions.assertEquals("EB2105", c.externalId)

        Concept narrower = taxonomy.getByExtId("EB210637")
        Assertions.assertNotNull(narrower)

        Assertions.assertNotNull(narrower.getParents())
        Assertions.assertEquals(1, narrower.getParents().size())
        Assertions.assertEquals(c, narrower.getParents()[0])
    }

    @Test
    void testEmbeddedRdf() {
        Taxonomy taxonomy = RdfReader.getTaxonomyFromXml("parser/rdf_embedded_sample.xml")

        Assertions.assertEquals(1, taxonomy.getTopConcepts().size())

        Concept c = taxonomy.getTopConcepts()[0]
        Assertions.assertNotNull(c.prefLabel)
        Assertions.assertTrue(c.prefLabel instanceof List)
        Assertions.assertEquals(1, c.prefLabel.size())
        Assertions.assertTrue(c.prefLabel.get(0) instanceof LanguageString)
        Assertions.assertEquals("Common concepts", c.prefLabel.get(0).getValue())
        Assertions.assertEquals("550", c.externalId)

        Concept narrower = taxonomy.getByExtId("6683")
        Assertions.assertNotNull(narrower)

        Assertions.assertNotNull(narrower.getParents())
        Assertions.assertEquals(1, narrower.getParents().size())
        Assertions.assertEquals(c, narrower.getParents()[0])
    }

    @Test
    void testEmbeddedRdf2() {
        Taxonomy taxonomy = RdfReader.getTaxonomyFromXml("parser/rdf_embedded_sample2.xml")

        Assertions.assertEquals(1, taxonomy.getTopConcepts().size())

        Concept c = taxonomy.getTopConcepts()[0]
        Assertions.assertNotNull(c.prefLabel)
        Assertions.assertTrue(c.prefLabel instanceof List)
        Assertions.assertEquals(1, c.prefLabel.size())
        Assertions.assertTrue(c.prefLabel.get(0) instanceof LanguageString)
        Assertions.assertEquals("Common concepts", c.prefLabel.get(0).getValue())
        Assertions.assertEquals("550", c.externalId)

        Concept narrower = taxonomy.getByExtId("5984")
        Assertions.assertNotNull(narrower)

        Assertions.assertNotNull(narrower.getParents())
        Assertions.assertEquals(1, narrower.getParents().size())
        Assertions.assertEquals(c, narrower.getParents()[0])

        Concept related = taxonomy.getByExtId("916")

        Assertions.assertNotNull(narrower.getRelated())
        Assertions.assertEquals(1, narrower.getRelated().size())
        Assertions.assertEquals(related, narrower.getRelated()[0])
    }

    @Test
    void testIdGeneration() {
        String externalId = "--[ Biological, Chemical & other Scientific Terms and Concepts ]--"
        Taxonomy taxonomy = RdfReader.getTaxonomyFromXml("parser/rdf_id_generation.xml")
        Assertions.assertEquals(1, taxonomy.getTopConcepts().size())

        Concept c = taxonomy.getByExtId(externalId)

        Assertions.assertEquals(c, taxonomy.getByLabel(externalId))
        Assertions.assertEquals(c, taxonomy.getByUri(c.getUri()))
    }

    @Test
    void testCircularRdf() {
        Taxonomy taxonomy = RdfReader.getTaxonomyFromXml("parser/rdf_circular_dependencies.xml")
        Assertions.assertEquals(1, taxonomy.getTopConcepts().size())

        List<Stack<Concept>> paths = TaxonomyUtils.getCircularDependencies(taxonomy)

        Assertions.assertEquals(1, paths.size())
        Assertions.assertEquals(4, paths.get(0).size())
    }
}

