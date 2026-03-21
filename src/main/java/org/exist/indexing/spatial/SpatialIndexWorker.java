package org.exist.indexing.spatial;

import org.exist.indexing.AbstractIndexWorker;
import org.exist.indexing.Index;
import org.exist.indexing.StreamListener;
import org.exist.storage.DBBroker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpatialIndexWorker extends AbstractIndexWorker {
    private static final Logger LOG = LogManager.getLogger(SpatialIndexWorker.class);
    private final SpatialStorageEngine storage;

    public SpatialIndexWorker(Index index, SpatialStorageEngine storage, DBBroker broker) {
        super(index, broker);
        this.storage = storage;
    }

    @Override
    public StreamListener getStreamListener() {
        return new SpatialStreamListener(storage, getDocument());
    }

    @Override
    public void removeDocument() {
        try {
            storage.deleteDocument(getDocument().getDocId());
            LOG.info("Spatial data removed for doc: " + getDocument().getDocId());
        } catch (Exception e) {
            LOG.error("Cleanup failed", e);
        }
    }

    @Override
    public void updateDocument() {
        removeDocument();
    }
}
