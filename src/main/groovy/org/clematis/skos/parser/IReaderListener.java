package org.clematis.skos.parser;

import org.clematis.skos.parser.model.Concept;
import org.clematis.skos.parser.model.Taxonomy;

/**
 * Listener for a streaming reader to listen to key events of SKOS file parsing
 */
public interface IReaderListener {
    /**
     * A concept created event
     * @param concept created
     */
    void conceptCreated(Concept concept);

    /**
     * A taxonomy created event
     * @param taxonomy created
     */
    void taxonomyCreated(Taxonomy taxonomy);
}
