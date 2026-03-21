package org.exist.indexing.spatial;

import org.exist.indexing.StreamListener;
import org.exist.dom.persistent.DocumentImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.gml2.GMLReader;

public class SpatialStreamListener implements StreamListener {
    private final SpatialStorageEngine storage;
    private final DocumentImpl doc;
    private StringBuilder buffer = new StringBuilder();
    private String currentSrs = "EPSG:4326";

    public SpatialStreamListener(SpatialStorageEngine storage, DocumentImpl doc) {
        this.storage = storage;
        this.doc = doc;
    }

    @Override
    public void startElement(org.exist.storage.txn.Txn transaction, org.exist.dom.persistent.ElementImpl element, org.exist.util.NodePath path) {
        String srs = element.getAttribute("srsName");
        if (srs != null) this.currentSrs = srs;
        
        if (element.getNamespaceURI().equals("http://www.opengis.net/gml")) {
            buffer.setLength(0); // On commence à capturer les coordonnées
        }
    }

    @Override
    public void endElement(org.exist.storage.txn.Txn transaction, org.exist.dom.persistent.ElementImpl element, org.exist.util.NodePath path) {
        if (element.getNamespaceURI().equals("http://www.opengis.net/gml") && buffer.length() > 0) {
            try {
                Geometry geom = new GMLReader().read(buffer.toString(), null);
                String nodeId = element.getNodeId().toString();
                String parentId = element.getParentNode().getNodeId().toString();

                // On indexe la géométrie principale
                storage.indexGeometry(doc, nodeId, parentId, geom, currentSrs, "GEOMETRY");

                // GESTION DES TROUS (Donuts)
                if (geom instanceof Polygon poly) {
                    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                        storage.indexGeometry(doc, nodeId + "_hole_" + i, nodeId, 
                            poly.getInteriorRingN(i), currentSrs, "INTERIOR_RING");
                    }
                }
            } catch (Exception e) { /* Log error */ }
        }
    }

    @Override
    public void characters(org.exist.storage.txn.Txn transaction, char[] ch, int start, int len) {
        buffer.append(ch, start, len);
    }
}
