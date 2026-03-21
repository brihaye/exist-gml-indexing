package org.exist.indexing.spatial;

import org.exist.indexing.AbstractIndex;
import org.exist.indexing.IndexWorker;
import org.exist.indexing.IndexController;
import org.exist.storage.DBBroker;
import org.exist.storage.BrokerPool;
import java.nio.file.Path;
import org.w3c.dom.Element;
import java.util.Map;

public class SpatialIndex extends AbstractIndex {

    private AbstractSpatialStore store;

    @Override
    public void configure(BrokerPool pool, Path path, Element config) {
        // Signature eXist 6 respectée
        ProjectionService projectionService = new ProjectionService();
        this.store = new BBoxOrientedSQLStore(projectionService);
    }

    @Override
    public boolean checkIndex(DBBroker broker) {
        // Doit renvoyer boolean pour eXist 6
        return true; 
    }

    @Override
    public IndexWorker getWorker(IndexController controller) {
        return new SpatialIndexWorker(this, controller);
    }

    public AbstractSpatialStore getStore() {
        return this.store;
    }

    @Override
    public void close() {
        if (store != null) {
            store.flush();
        }
    }
}
