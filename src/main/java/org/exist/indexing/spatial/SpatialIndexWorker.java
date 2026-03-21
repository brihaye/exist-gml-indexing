package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.indexing.AbstractIndexWorker;
import org.exist.indexing.IndexController;
import org.exist.indexing.Occurrences; // <--- C'est ici ! (Pas org.exist.xquery)
import org.exist.storage.txn.Txn;

public class SpatialIndexWorker extends AbstractIndexWorker {

    private final SpatialIndex index;

    public SpatialIndexWorker(SpatialIndex index, IndexController controller) {
        super(index, controller);
        this.index = index;
    }

    @Override
    public String getIndexId() {
        return "http://exist-db.org/indexing/spatial";
    }

    @Override
    public void occurrence(Txn transaction, NodeProxy node, Occurrences occurrences) {
        String value = node.getStringValue();
        if (value != null && value.contains("gml:")) {
            index.getStore().addGeometry(transaction, node, value);
        }
    }

    @Override
    public void remove(Txn transaction, DocumentImpl document) {
        index.getStore().removeDocument(transaction, document);
    }

    @Override
    public void flush() {
        if (index.getStore() != null) {
            index.getStore().flush();
        }
    }
}
