package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.indexing.IndexWorker; // On utilise l'interface directement
import org.exist.storage.txn.Txn;
import org.exist.storage.DBBroker;

// On implémente IndexWorker directement au lieu d'AbstractIndexWorker
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

    // On retire l'argument Occurrences qui pose problème
    // et on simplifie la méthode selon l'interface eXist 6
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
    
    // Si Maven hurle sur l'absence de méthodes, il nous donnera la liste exacte au prochain tour
}
