package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeImpl;
import org.exist.indexing.StreamListener;
import org.exist.storage.NodePath;
import org.exist.storage.txn.Txn;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.gml2.GMLReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpatialStreamListener implements StreamListener {
    private static final Logger LOG = LogManager.getLogger(SpatialStreamListener.class);
    private final SpatialStorageEngine storage;
    private final DocumentImpl doc;
    private StringBuilder buffer = new StringBuilder();
    private String currentSrs = "EPSG:4326";

    public SpatialStreamListener(SpatialStorageEngine storage, DocumentImpl doc) {
        this.storage = storage;
        this.doc = doc;
    }

    @Override
    public void startElement(Txn transaction, org.exist.dom.persistent.ElementImpl element, NodePath path) {
        String srs = element.getAttribute("srsName");
        if (srs != null) this.currentSrs = srs;
        
        if (element.getNamespaceURI().equals("http://www.opengis.net/gml")) {
            buffer.setLength(0);
        }
    }

    @Override
    public void endElement(Txn transaction, org.exist.dom.persistent.ElementImpl element, NodePath path) {
        if (element.getNamespaceURI().equals("http://www.opengis.net/gml") && buffer.length() > 0) {
            try {
                Geometry geom = new GMLReader().read(buffer.toString(), null);
                // Cast obligatoire en NodeImpl pour accéder à getNodeId()
                String nodeId = ((NodeImpl)element).getNodeId().toString();
                String parentId = ((NodeImpl)element.getParentNode()).getNodeId().toString();

                storage.indexGeometry(doc, nodeId, parentId, geom, currentSrs, "GEOMETRY");

                if (geom instanceof Polygon poly) {
                    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                        storage.indexGeometry(doc, nodeId + "_h" + i, nodeId, 
                            poly.getInteriorRingN(i), currentSrs, "HOLE");
                    }
                }
            } catch (Exception e) {
                LOG.error("GML Parsing error", e);
            }
        }
    }

    @Override
    public void characters(Txn transaction, char[] ch, int start, int len) {
        buffer.append(ch, start, len);
    }

    @Override
    public void endIndexDocument(Txn transaction) {
        // Méthode obligatoire en eXist 6
    }
}
