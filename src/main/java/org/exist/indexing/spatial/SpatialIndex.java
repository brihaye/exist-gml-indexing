package org.exist.indexing.spatial;

import org.exist.indexing.AbstractIndex;
import org.exist.indexing.IndexWorker;
import org.exist.storage.DBBroker;
import org.exist.storage.BrokerPool;
import java.nio.file.Path;
import org.w3c.dom.Element;

public class SpatialIndex extends AbstractIndex {

    private AbstractSpatialStore store;

    @Override
    public void configure(BrokerPool pool, Path path, Element config) {
        ProjectionService projectionService = new ProjectionService();
        this.store = new BBoxOrientedSQLStore(projectionService);
    }

    @Override
    public boolean checkIndex(DBBroker broker) {
        return true; 
    }

    // LA SIGNATURE QUE MAVEN RÉCLAME : DBBroker au lieu de IndexController
    @Override
    public IndexWorker getWorker(DBBroker broker) {
        return new SpatialIndexWorker(this, broker);
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
