package org.exist.indexing.spatial;

import org.exist.indexing.AbstractIndexWorker;
import org.exist.indexing.StreamListener;
import org.exist.dom.persistent.DocumentImpl;
import org.exist.storage.DBBroker;

public class SpatialIndexWorker extends AbstractIndexWorker {
    private final SpatialStorageEngine storage;

    public SpatialIndexWorker(SpatialIndex index, SpatialStorageEngine storage, DBBroker broker) {
        super(index, broker);
        this.storage = storage;
    }

    @Override
    public StreamListener getStreamListener() {
        // Chaque indexation de document reçoit un nouveau Listener
        return new SpatialStreamListener(storage, getDocument());
    }

    @Override
    public void removeDocument() {
        try {
            // NETTOYAGE : Invoqué par eXist lors d'une suppression de document
            storage.deleteDocument(getDocument().getDocId());
        } catch (Exception e) {
            LOG.error("Sync error: SQL cleanup failed", e);
        }
    }

    @Override
    public boolean checkIndex(DBBroker broker) {
        // Vérification de cohérence (Garbage Collection)
        // [Implémentation de parcours des DOC_ID orphelins comme discuté]
        return true; 
    }
}
