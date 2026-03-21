package org.exist.indexing.spatial;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.io.WKBWriter;
import java.sql.*;
import java.util.Map;

public class HSQLSpatialIndex implements SpatialStorageEngine {
    private Connection connection;
    private WKBWriter wkbWriter = new WKBWriter();

    @Override
    public void initialize(Map<String, String> params) throws Exception {
        String dbPath = params.getOrDefault("db-path", "data/spatial_hsql");
        Class.forName("org.hsqldb.jdbcDriver");
        this.connection = DriverManager.getConnection("jdbc:hsqldb:file:" + dbPath, "SA", "");
        
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS SPATIAL_INDEX (" +
                "DOC_ID BIGINT, NODE_ID VARCHAR(255), PARENT_ID VARCHAR(255), " +
                "GEOM_TYPE VARCHAR(50), SRS_ORIGIN VARCHAR(50), " +
                "MINX_ORG DOUBLE, MINY_ORG DOUBLE, MAXX_ORG DOUBLE, MAXY_ORG DOUBLE, GEOM_ORG BLOB, " +
                "MINX_WGS84 DOUBLE, MINY_WGS84 DOUBLE, MAXX_WGS84 DOUBLE, MAXY_WGS84 DOUBLE, GEOM_WGS84 BLOB)");
            st.execute("CREATE INDEX IF NOT EXISTS IDX_DOC ON SPATIAL_INDEX (DOC_ID)");
            st.execute("CREATE INDEX IF NOT EXISTS IDX_WGS ON SPATIAL_INDEX (MINX_WGS84, MAXX_WGS84, MINY_WGS84, MAXY_WGS84)");
        }
    }

    @Override
    public void indexGeometry(org.exist.dom.persistent.DocumentImpl doc, String nodeId, String parentId, 
                             Geometry geom, String srs, String type) throws Exception {
        
        // Ta vision : Transformation WGS84 (ici simplifiée pour l'exemple)
        Geometry geomWGS84 = CoordinateTransformer.transform(geom, srs, "EPSG:4326");
        Envelope envOrg = geom.getEnvelopeInternal();
        Envelope envWGS = geomWGS84.getEnvelopeInternal();

        String sql = "INSERT INTO SPATIAL_INDEX VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, doc.getDocId());
            ps.setString(2, nodeId);
            ps.setString(3, parentId);
            ps.setString(4, type);
            ps.setString(5, srs);
            // Original MBR & Blob
            ps.setDouble(6, envOrg.getMinX()); ps.setDouble(7, envOrg.getMinY());
            ps.setDouble(8, envOrg.getMaxX()); ps.setDouble(9, envOrg.getMaxY());
            ps.setBytes(10, wkbWriter.write(geom));
            // WGS84 MBR & Blob
            ps.setDouble(11, envWGS.getMinX()); ps.setDouble(12, envWGS.getMinY());
            ps.setDouble(13, envWGS.getMaxX()); ps.setDouble(14, envWGS.getMaxY());
            ps.setBytes(15, wkbWriter.write(geomWGS84));
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteDocument(long docId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM SPATIAL_INDEX WHERE DOC_ID = ?")) {
            ps.setLong(1, docId);
            ps.executeUpdate();
        }
    }

    @Override public Connection getConnection() { return connection; }
    @Override public void shutdown() throws Exception { if(connection != null) connection.close(); }
}
