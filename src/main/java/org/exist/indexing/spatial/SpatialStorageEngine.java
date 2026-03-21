package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.locationtech.jts.geom.Geometry;
import java.util.Map;

public interface SpatialStorageEngine {
    void initialize(Map<String, String> params) throws Exception;
    
    // Indexation avec gestion du parent et du type (ex: HOLE, SHELL)
    void indexGeometry(DocumentImpl doc, String nodeId, String parentId, 
                       Geometry geom, String srsName, String type) throws Exception;
    
    void deleteDocument(long docId) throws Exception;
    
    void shutdown() throws Exception;
    
    // Pour la synchronisation et le nettoyage
    java.sql.Connection getConnection() throws Exception;
}
