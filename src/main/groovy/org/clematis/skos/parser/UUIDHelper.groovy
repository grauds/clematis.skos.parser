package org.clematis.skos.parser

import java.util.concurrent.ConcurrentHashMap
import org.clematis.skos.parser.model.IdAware
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UUIDHelper {

    private static final Logger LOG = LoggerFactory.getLogger(UUIDHelper.class)
    private final static ConcurrentHashMap<String, String> generatedIdsPool = new ConcurrentHashMap<>()

    static synchronized String findKey(String value) {
        for (Map.Entry<String, String> entry : generatedIdsPool.entrySet()) {
            if (entry.getValue().equals(IdAware.formatToURI(value))) {
                return entry.getKey()
            }
        }
        return value
    }

    static synchronized String title2UUID(String title) {

        String uuid

        if (!generatedIdsPool.containsKey(title)) {
            uuid = UUID.randomUUID().toString()
            generatedIdsPool.put(title, uuid)
            LOG.info("Generated " + uuid + " for " + title)
        } else {
            uuid = generatedIdsPool.get(title)
            LOG.info("Found " + uuid + " for " + title)
        }

        return uuid
    }

    static Map<String, String> getGeneratedIdsPool() {
        generatedIdsPool
    }
}
