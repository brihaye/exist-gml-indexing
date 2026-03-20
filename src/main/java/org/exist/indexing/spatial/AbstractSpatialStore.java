package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.storage.txn.Txn;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.gml2.GMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Store spatial de base utilisant JTS et compatible Txn (eXist 6.x)
 */
public abstract class AbstractSpatialStore {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpatialStore.class);
    private final GMLReader gmlReader = new GMLReader();
    
    // Cache temporaire des géométries
    protected final ConcurrentHashMap<Long, Geometry> geometryCache = new ConcurrentHashMap<>();

    public void addGeometry(Txn transaction, NodeProxy node, String gml) {
        try {
            Geometry geometry = gmlReader.read(gml, null);
            if (geometry != null) {
                // On stocke la géométrie liée à l'ID du nœud
                geometryCache.put(node.getNodeId().getCoordinate(), geometry);
                LOG.debug("Indexed geometry for node {}", node.getNodeId());
            }
        } catch (Exception e) {
            LOG.error("Failed to parse GML for node: " + node.getNodeId(), e);
        }
    }

    public void removeDocument(Txn transaction, DocumentImpl document) {
        // Nettoyage lors de la suppression d'un doc
        LOG.info("Removing spatial index for document {}", document.getURI());
        geometryCache.clear(); // Simplification pour le test
    }

    public abstract void flush();
    
    // Cette méthode sera appelée par nos tests JUnit
    public boolean contains(long nodeId, Geometry other) {
        Geometry geom = geometryCache.get(nodeId);
        return geom != null && geom.contains(other);
    }
}
