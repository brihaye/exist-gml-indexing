package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.indexing.AbstractIndexWorker;
import org.exist.indexing.IndexController;
import org.exist.storage.txn.Txn;
import org.exist.xquery.index.Occurrences; // Changement d'adresse pour Occurrences

/**
 * Worker spatial mis à jour pour eXist-db 6.2.0
 */
public class SpatialIndexWorker extends AbstractIndexWorker {

    private final SpatialIndex index;

    public SpatialIndexWorker(SpatialIndex index, IndexController controller) {
        // Dans eXist 6, le constructeur attend l'index et le contrôleur
        super(index, controller);
        this.index = index;
    }

    @Override
    public String getIndexId() {
        return "http://exist-db.org/indexing/spatial";
    }

    @Override
    public void occurrence(Txn transaction, NodeProxy node, Occurrences occurrences) {
        // Extraction de la géométrie GML
        String gml = node.getStringValue();
        if (gml != null && (gml.contains("gml:") || gml.contains("<Geometry"))) {
            index.getStore().addGeometry(transaction, node, gml);
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
