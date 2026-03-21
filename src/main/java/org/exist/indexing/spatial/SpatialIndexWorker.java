package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.DocumentSet;
import org.exist.dom.persistent.NodeSet;
import org.exist.dom.persistent.NodeProxy;
import org.exist.indexing.IndexWorker;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;
import org.exist.xquery.XQueryContext;
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

    @Override
    public Object getQueryRewriter(XQueryContext context) {
        // En renvoyant Object, on évite l'erreur d'import de QueryRewriter
        return null;
    }

    @Override
    public void scanIndex(XQueryContext context, DocumentSet docs, NodeSet nodes, Map<?, ?> params) {
        // Obligatoire en eXist 6, peut rester vide pour l'instant
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
