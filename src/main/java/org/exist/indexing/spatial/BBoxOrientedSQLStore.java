package org.exist.indexing.spatial;

import org.locationtech.jts.geom.Envelope;
import java.sql.*;
import java.util.Map;

/**
 * Connecteur spécifique pour HSQLDB / SQL.
 */
public class BBoxOrientedSQLStore extends AbstractSpatialStore {
    
    private Connection conn;

    public BBoxOrientedSQLStore(ProjectionService ps) {
        super(ps);
    }

    @Override
    public void init(Map<String, String> params) throws Exception {
        String dbPath = params.getOrDefault("data-dir", "webapp/WEB-INF/data/spatial-index");
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        conn = DriverManager.getConnection("jdbc:hsqldb:file:" + dbPath + ";shutdown=true", "SA", "");
        conn.setAutoCommit(false);

        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS spatial_index (" +
                       "node_id VARCHAR(255), part_idx INTEGER, srs_id VARCHAR(20), " +
                       "minx DOUBLE, maxx DOUBLE, miny DOUBLE, maxy DOUBLE)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_spatial_range ON spatial_index (minx, maxx, miny, maxy)");
        }
    }

    @Override
    protected void saveToBackend(String id, int idx, String srs, Envelope bbox, long txnId) throws Exception {
        String sql = "INSERT INTO spatial_index VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setInt(2, idx);
            ps.setString(3, srs);
            ps.setDouble(4, bbox.getMinX());
            ps.setDouble(5, bbox.getMaxX());
            ps.setDouble(6, bbox.getMinY());
            ps.setDouble(7, bbox.getMaxY());
            ps.executeUpdate();
        }
    }

    @Override public void commit(long txnId) throws Exception { if(conn != null) conn.commit(); }
    @Override public void rollback(long txnId) throws Exception { if(conn != null) conn.rollback(); }
    @Override public void shutdown() throws Exception { if(conn != null) { conn.commit(); conn.close(); } }
}
