package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.indexing.AbstractIndexWorker;
import org.exist.indexing.IndexController;
import org.exist.indexing.Occurrences; // Import mis à jour pour eXist 6
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
        // On vérifie que le store existe avant d'appeler addGeometry
        if (value != null && value.contains("gml:") && index.getStore() != null) {
            index.getStore().addGeometry(transaction, node, value);
        }
    }

    @Override
    public void remove(Txn transaction, DocumentImpl document) {
        if (index.getStore() != null) {
            index.getStore().removeDocument(transaction, document);
        }
    }

    @Override
    public void flush() {
        if (index.getStore() != null) {
            index.getStore().flush();
        }
    }
}
