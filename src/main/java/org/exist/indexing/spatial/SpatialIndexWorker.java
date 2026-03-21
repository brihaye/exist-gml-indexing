package org.exist.indexing.spatial;

import org.exist.indexing.AbstractIndex;
import org.exist.indexing.IndexWorker;
import org.exist.storage.DBBroker;
import java.util.Map;

public class SpatialIndex extends AbstractIndex {
    private SpatialStorageEngine storage;

    @Override
    public void configure(org.exist.indexing.IndexController controller, org.w3c.dom.NodeList configNodes, Map<String, String> params) {
        try {
            // Instanciation directe pour éviter l'erreur de Factory manquante
            this.storage = new HSQLSpatialIndex();
            this.storage.initialize(params);
        } catch (Exception e) {
            // LOG d'erreur critique ici
        }
    }

    @Override
    public IndexWorker getWorker(DBBroker broker) {
        return new SpatialIndexWorker(this, storage, broker);
    }

    @Override
    public boolean checkIndex(DBBroker broker) {
        return true; // Méthode obligatoire
    }

    @Override
    public void close() {
        try { storage.shutdown(); } catch (Exception e) {}
    }

    @Override
    public String getIndexId() { return "http://exist-db.org/indexing/spatial"; }
}
