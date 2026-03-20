package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.indexing.AbstractIndexWorker;
import org.exist.indexing.IndexController;
import org.exist.storage.txn.Txn; // Changement majeur : Txn au lieu de Transaction
import org.exist.xquery.value.Type;
import org.exist.xquery.Occurrences;

import java.util.Map;

/**
 * Worker pour l'indexation spatiale compatible eXist-db 6.x
 */
public class SpatialIndexWorker extends AbstractIndexWorker {

    private final SpatialIndex index;

    public SpatialIndexWorker(SpatialIndex index, IndexController controller) {
        super(index, controller);
        this.index = index;
    }

    @Override
    public String getIndexId() {
        return SpatialIndex.ID;
    }

    @Override
    public void occurrence(Txn transaction, NodeProxy node, Occurrences occurrences) {
        // Logique d'indexation lors de la lecture d'un nœud GML
        String gml = node.getStringValue();
        if (gml != null && gml.contains("gml:")) {
            index.getStore().addGeometry(transaction, node, gml);
        }
    }

    @Override
    public void remove(Txn transaction, DocumentImpl document) {
        index.getStore().removeDocument(transaction, document);
    }

    @Override
    public void flush() {
        index.getStore().flush();
    }
}
