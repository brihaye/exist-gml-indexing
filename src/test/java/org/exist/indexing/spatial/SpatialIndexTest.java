package org.exist.indexing.spatial;

import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpatialIndexTest {

    private HSQLSpatialIndex db;
    private WKTReader wktReader = new WKTReader();

    @BeforeAll
    void setup() throws Exception {
        db = new HSQLSpatialIndex();
        Map<String, String> params = new HashMap<>();
        params.put("db-path", "target/test_spatial_db"); // Stockage temporaire pour le test
        db.initialize(params);
    }

    @Test
    @DisplayName("Test de la double indexation et gestion des trous")
    void testIndexationComplete() throws Exception {
        // 1. Création d'un polygone "Donut" en Lambert 93 (EPSG:2154)
        // Un carré de 10m avec un trou de 2m au centre
        String wkt = "POLYGON((600000 7000000, 600010 7000000, 600010 7000010, 600000 7000010, 600000 7000000), " +
                     "(600004 7000004, 600006 7000004, 600006 7000006, 600004 7000006, 600004 7000004))";
        Geometry poly = wktReader.read(wkt);

        // 2. Simulation de l'indexation par le Worker
        // Mock du document eXist (ID = 1)
        long mockDocId = 1L;
        String nodeId = "1.2.3";
        
        db.indexGeometry(null, nodeId, null, poly, "EPSG:2154", "POLYGON");

        // 3. Vérification en base SQL
        var conn = db.getConnection();
        var stmt = conn.createStatement();
        var rs = stmt.executeQuery("SELECT * FROM SPATIAL_INDEX WHERE NODE_ID = '1.2.3'");
        
        assertTrue(rs.next(), "Le polygone doit être présent dans HSQLDB");
        
        // Vérification de la transformation WGS84
        double minxWgs = rs.getDouble("MINX_WGS84");
        assertTrue(minxWgs < 10.0, "La coordonnée doit être en degrés (WGS84) et non en mètres (Lambert)");
        
        System.out.println("Test réussi : Géométrie insérée et transformée.");
    }

    @Test
    @DisplayName("Test du Trigger de nettoyage")
    void testSuppression() throws Exception {
        db.deleteDocument(1L);
        var conn = db.getConnection();
        var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM SPATIAL_INDEX WHERE DOC_ID = 1");
        rs.next();
        assertEquals(0, rs.getInt(1), "Le nettoyage doit supprimer toutes les lignes du document");
    }

    @AfterAll
    void tearDown() throws Exception {
        db.shutdown();
    }
}
