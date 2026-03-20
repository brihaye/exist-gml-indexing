package org.exist.indexing.spatial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

@DisplayName("Validation Globale de l'Index Spatial GML")
class SpatialIndexGlobalTest {

    private static WKTReader reader;
    private static Geometry square; // Carré 10x10 (0,0 à 10,10)

    @BeforeAll
    static void setup() throws Exception {
        reader = new WKTReader();
        square = reader.read("POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))");
    }

    @Nested
    @DisplayName("Tests d'Intersection (Intersects)")
    class IntersectTests {

        @Test
        @DisplayName("OUI : Point au centre (5,5)")
        void testPointInside() throws Exception {
            assertTrue(square.intersects(reader.read("POINT(5 5)")));
        }

        @Test
        @DisplayName("NON : Point à l'extérieur (15,15)")
        void testPointOutside() throws Exception {
            assertFalse(square.intersects(reader.read("POINT(15 15)")));
        }

        @Test
        @DisplayName("LIMITE : Point sur le bord (10,5)")
        void testPointOnBoundary() throws Exception {
            assertTrue(square.intersects(reader.read("POINT(10 5)")));
        }

        @Test
        @DisplayName("LIMITE : Point sur un sommet (0,0)")
        void testPointOnVertex() throws Exception {
            assertTrue(square.intersects(reader.read("POINT(0 0)")));
        }
        
        @Test
        @DisplayName("PIÈGE : Précision infinitésimale (10.0000001)")
        void testFloatingPointPrecision() throws Exception {
            assertFalse(square.intersects(reader.read("POINT(10.0000001 5)")));
        }
    }

    @Nested
    @DisplayName("Tests de Contenance (Within/Contains)")
    class ContainmentTests {

        @Test
        @DisplayName("OUI : Ligne totalement à l'intérieur")
        void testLineWithin() throws Exception {
            assertTrue(reader.read("LINESTRING(1 1, 2 2)").within(square));
        }

        @Test
        @DisplayName("NON : Ligne qui dépasse")
        void testLineCrossing() throws Exception {
            assertFalse(reader.read("LINESTRING(5 5, 15 15)").within(square));
        }

        @Test
        @DisplayName("LIMITE : Point sur le bord n'est pas 'Within'")
        void testBoundaryIsNotWithin() throws Exception {
            // Un point sur la ligne n'est pas strictement "à l'intérieur"
            assertFalse(reader.read("POINT(10 5)").within(square));
        }
    }

    @Nested
    @DisplayName("Tests de Topologie Complexe (Donut)")
    class ComplexTopologyTests {

        @Test
        @DisplayName("PIÈGE : Point dans le trou d'un Donut")
        void testPointInHole() throws Exception {
            // Carré avec un trou de 4x4 au milieu
            Geometry donut = reader.read("POLYGON((0 0, 10 0, 10 10, 0 10, 0 0), (3 3, 3 7, 7 7, 7 3, 3 3))");
            assertFalse(donut.contains(reader.read("POINT(5 5)")), "Le trou ne doit pas contenir le point");
        }
    }
}
