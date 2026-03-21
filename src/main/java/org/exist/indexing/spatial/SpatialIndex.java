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
            // Agnosticisme : on crée le moteur défini en config
            this.storage = SpatialStoreFactory.createInternal(params);
        } catch (Exception e) { /* Critical log */ }
    }

    @Override
    public IndexWorker getWorker(DBBroker broker) {
        return new SpatialIndexWorker(this, storage, broker);
    }

    @Override
    public void close() {
        try { storage.shutdown(); } catch (Exception e) {}
    }

    @Override
    public String getIndexId() { return "http://exist-db.org/indexing/spatial"; }
}
