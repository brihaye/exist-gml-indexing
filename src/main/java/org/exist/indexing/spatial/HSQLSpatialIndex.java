package org.exist.indexing.spatial;

import java.sql.*;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.exist.dom.persistent.DocumentImpl;

public class HSQLSpatialIndex implements SpatialStorageEngine {
    private static final Logger LOG = LogManager.getLogger(HSQLSpatialIndex.class);
    private Connection connection;

    @Override
    public void initialize(Map<String, String> params) throws Exception {
        String dbPath = params.getOrDefault("db-path", "webapp/WEB-INF/data/spatial_index/hsql");
        
        // Création du dossier si inexistant
        java.io.File dir = new java.io.File(dbPath).getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();

        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        this.connection = DriverManager.getConnection("jdbc:hsqldb:file:" + dbPath + ";shutdown=true", "SA", "");
        
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS SPATIAL_INDEX (" +
                       "DOC_ID INT, NODE_ID VARCHAR(255), PARENT_ID VARCHAR(255), " +
                       "WKT_WGS84 CLOB, SRS VARCHAR(50), TYPE VARCHAR(20))");
        }
        LOG.info("HSQLDB Spatial Storage initialized at {}", dbPath);
    }

    @Override
    public void indexGeometry(DocumentImpl doc, String nodeId, String parentId, Geometry geom, String srs, String type) throws Exception {
        // Transformation simple en WKT pour HSQL (en attendant une vraie extension spatiale)
        String query = "INSERT INTO SPATIAL_INDEX (DOC_ID, NODE_ID, PARENT_ID, WKT_WGS84, SRS, TYPE) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, doc.getDocId());
            ps.setString(2, nodeId);
            ps.setString(3, parentId);
            ps.setString(4, geom.toText()); // WKT
            ps.setString(5, srs);
            ps.setString(6, type);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteDocument(int docId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM SPATIAL_INDEX WHERE DOC_ID = ?")) {
            ps.setInt(1, docId);
            ps.executeUpdate();
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.createStatement().execute("SHUTDOWN");
            connection.close();
        }
    }
}
