package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.indexing.IndexWorker;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;

public class SpatialIndexWorker implements IndexWorker {

    private final SpatialIndex index;
    private final DBBroker broker;

    public SpatialIndexWorker(SpatialIndex index, DBBroker broker) {
        this.index = index;
        this.broker = broker;
    }

    @Override
    public String getIndexId() {
        return "http://exist-db.org/indexing/spatial";
    }

    @Override
    public void flush() {
        if (index.getStore() != null) {
            index.getStore().flush();
        }
    }

    @Override
    public void remove(Txn transaction, DocumentImpl document) {
        if (index.getStore() != null) {
            index.getStore().removeDocument(transaction, document);
        }
    }
}
