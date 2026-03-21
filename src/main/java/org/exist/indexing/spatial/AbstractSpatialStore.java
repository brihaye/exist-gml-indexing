package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.storage.txn.Txn;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.gml2.GMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public abstract class AbstractSpatialStore {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpatialStore.class);
    protected final GMLReader gmlReader = new GMLReader();
    
    protected AbstractSpatialStore(ProjectionService projectionService) {}

    public abstract void init(Map<String, String> params) throws Exception;

    public void addGeometry(Txn txn, NodeProxy node, String gmlString) {
        try {
            Geometry geometry = gmlReader.read(gmlString, new GeometryFactory());
            if (geometry != null) {
                saveToPersistentStore(txn, node.getNodeId().toString(), geometry);
            }
        } catch (Exception e) {
            LOG.error("Erreur GML", e);
        }
    }

    public void removeDocument(Txn txn, DocumentImpl doc) {
        removeFromPersistentStore(txn, doc);
    }

    public abstract void flush();
    public abstract void shutdown() throws Exception;

    protected abstract void saveToPersistentStore(Txn txn, String nodeId, Geometry geom);
    protected abstract void removeFromPersistentStore(Txn txn, DocumentImpl doc);
}
