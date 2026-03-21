package org.exist.indexing.spatial;

import org.exist.indexing.AbstractIndex;
import org.exist.indexing.IndexWorker;
import org.exist.indexing.IndexController;
import java.util.Map;

public class SpatialIndex extends AbstractIndex {

    // On utilise AbstractSpatialStore comme base commune
    private AbstractSpatialStore store;

    @Override
    public void configure(IndexController controller, Map<String, String> params) {
        super.configure(controller, params);
        // Initialisation du store (exemple avec le BBox Store)
        ProjectionService projectionService = new ProjectionService();
        this.store = new BBoxOrientedSQLStore(projectionService);
    }

    @Override
    public IndexWorker getWorker(IndexController controller) {
        return new SpatialIndexWorker(this, controller);
    }

    // UNE SEULE FOIS cette méthode pour que le Worker puisse y accéder
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
