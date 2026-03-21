package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.DocumentSet;
import org.exist.dom.persistent.NodeSet;
import org.exist.indexing.IndexWorker;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.QueryRewriter;
import org.exist.util.Occurrences;
import java.util.Map;

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

    // NOUVELLE MÉTHODE RÉCLAMÉE PAR MAVEN
    @Override
    public boolean checkIndex(DBBroker broker) {
        return true;
    }

    @Override
    public QueryRewriter getQueryRewriter(XQueryContext context) {
        return null;
    }

    @Override
    public Occurrences[] scanIndex(XQueryContext context, DocumentSet docs, NodeSet nodes, Map<?, ?> params) {
        return null;
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
