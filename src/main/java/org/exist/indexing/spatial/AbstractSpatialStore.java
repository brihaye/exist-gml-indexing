package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.storage.txn.Txn;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.gml2.GMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public abstract class AbstractSpatialStore {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpatialStore.class);
    protected final GMLReader gmlReader = new GMLReader();
    
    // On passe en String pour la clé car getCoordinate() a disparu en eXist 6
    protected final Map<String, Geometry> internalCache = new ConcurrentHashMap<>();

    // Ajout d'un constructeur qui accepte la projection pour éviter l'erreur dans BBoxOrientedSQLStore
    protected AbstractSpatialStore(ProjectionService projectionService) {
        // Initialisation si besoin
    }

    public void addGeometry(Txn txn, NodeProxy node, String gmlString) {
        try {
            Geometry geometry = gmlReader.read(gmlString);
            if (geometry != null) {
                // Utilisation de toString() car getCoordinate() n'existe plus
                String nodeId = node.getNodeId().toString();
                internalCache.put(nodeId, geometry);
                saveToPersistentStore(txn, nodeId, geometry);
            }
        } catch (Exception e) {
            LOG.error("Erreur GML pour le nœud " + node.getNodeId(), e);
        }
    }

    public abstract void flush();
    
    // On met à jour la signature ici aussi (String au lieu de long)
    protected abstract void saveToPersistentStore(Txn txn, String nodeId, Geometry geom);
    protected abstract void removeFromPersistentStore(Txn txn, DocumentImpl doc);
}
