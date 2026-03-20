AbstractSpatialStore.javapackage org.exist.indexing.spatial;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;

public abstract class AbstractSpatialStore implements SpatialStore {
    
    protected final ProjectionService projectionService;

    protected AbstractSpatialStore(ProjectionService ps) {
        this.projectionService = ps;
    }

    @Override
    public void addGeometryPart(String nodeId, Geometry part, int idx, String srsId, long txnId) throws Exception {
        // Logique commune à TOUS les supports (SQL, JSON, Shp...)
        Envelope env = part.getEnvelopeInternal();
        double padding = projectionService.getPadding(env.centre(), srsId);
        env.expandBy(padding);

        // On délègue le stockage réel à l'implémentation spécifique
        saveToBackend(node_id, idx, srsId, env, txnId);
    }

    protected abstract void saveToBackend(String id, int idx, String srs, Envelope bbox, long txnId) throws Exception;
}
