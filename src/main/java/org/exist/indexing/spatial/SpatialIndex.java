package org.exist.indexing.spatial;

import org.locationtech.jts.geom.Geometry;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Point d'entrée principal pour l'indexeur spatial eXist-db.
 */
public class SpatialIndex {

    private final SpatialStore store;
    private final UniversalGMLHandler handler;

    public SpatialIndex(Map<String, String> params) throws Exception {
        // Initialisation de la chaîne de composants
        ProjectionService ps = new ProjectionService();
        this.store = new BBoxOrientedSQLStore(ps);
        this.handler = new UniversalGMLHandler(ps);
        
        this.store.init(params);
    }

    /**
     * Méthode appelée par eXist lors du passage sur un nœud GML
     */
    public void index(String nodeId, XMLStreamReader reader, long txnId) throws Exception {
        // 1. On parse le GML pour obtenir une géométrie JTS (éventuellement MultiPart)
        Geometry geom = handler.parse(reader);
        
        if (geom != null) {
            // 2. On décompose et on stocke chaque partie (Polygone simple)
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                store.addGeometryPart(nodeId, geom.getGeometryN(i), i, null, txnId);
            }
        }
    }

    public void commit(long txnId) throws Exception { store.commit(txnId); }
    public void shutdown() throws Exception { store.shutdown(); }
}
