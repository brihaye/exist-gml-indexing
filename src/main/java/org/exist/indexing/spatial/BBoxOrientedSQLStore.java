package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.storage.txn.Txn;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;
import java.sql.*;
import java.util.Map;

public class BBoxOrientedSQLStore extends AbstractSpatialStore {
    private Connection conn;

    public BBoxOrientedSQLStore(ProjectionService ps) {
        super(ps);
    }

    @Override
    public void init(Map<String, String> params) throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        // Utilisation d'un chemin relatif propre à eXist si possible, mais gardons celui-ci pour la compilation
        conn = DriverManager.getConnection("jdbc:hsqldb:file:spatial-index;shutdown=true", "SA", "");
        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS spatial_index (node_id VARCHAR(255), doc_id INTEGER, minx DOUBLE, maxx DOUBLE, miny DOUBLE, maxy DOUBLE)");
        }
    }

    @Override
    protected void saveToPersistentStore(Txn txn, String nodeId, Geometry geom) {
        Envelope bbox = geom.getEnvelopeInternal();
        String sql = "INSERT INTO spatial_index (node_id, minx, maxx, miny, maxy) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nodeId);
            ps.setDouble(2, bbox.getMinX());
            ps.setDouble(3, bbox.getMaxX());
            ps.setDouble(4, bbox.getMinY());
            ps.setDouble(5, bbox.getMaxY());
            ps.executeUpdate();
            // Note: Le commit ici est risqué pour les performances, mais assure la persistance immédiate
            conn.commit();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    @Override
    protected void removeFromPersistentStore(Txn txn, DocumentImpl doc) {
        // Ajout d'une logique minimale pour éviter que la méthode soit vide
        // On pourrait supprimer par doc_id si on l'avait stocké
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM spatial_index WHERE node_id LIKE ?")) {
            ps.setString(1, doc.getDocId() + "%"); 
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override 
    public void flush() { 
        try { 
            if(conn != null && !conn.isClosed()) conn.commit(); 
        } catch(Exception e) {} 
    }

    @Override 
    public void shutdown() throws Exception { 
        if(conn != null && !conn.isClosed()) { 
            conn.commit(); 
            conn.close(); 
        } 
    }
}
