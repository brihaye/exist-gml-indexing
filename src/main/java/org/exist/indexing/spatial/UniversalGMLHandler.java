package org.exist.indexing.spatial;

import org.locationtech.jts.geom.*;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler universel GML 2.x / 3.x pour eXist-db.
 * Gère la décomposition en parties et la topologie (trous).
 */
public class UniversalGMLHandler {

    private final GeometryFactory gf = new GeometryFactory();
    private final ProjectionService projectionService;

    public UniversalGMLHandler(ProjectionService ps) {
        this.projectionService = ps;
    }

    /**
     * Point d'entrée pour parser une géométrie depuis le flux StAX d'eXist.
     */
    public Geometry parse(XMLStreamReader reader) throws XMLStreamException {
        // On cherche le premier élément de géométrie (Point, Polygon, etc.)
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                switch (localName) {
                    case "Point": return parsePoint(reader);
                    case "LineString": return parseLineString(reader);
                    case "Polygon": return parsePolygon(reader);
                    case "MultiPolygon":
                    case "MultiSurface":
                        return parseMultiGeometry(reader, localName);
                }
            }
            if (event == XMLStreamConstants.END_ELEMENT) break;
        }
        return null;
    }

    private Polygon parsePolygon(XMLStreamReader reader) throws XMLStreamException {
        LinearRing shell = null;
        List<LinearRing> holes = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = reader.getLocalName();
                // Support mixte GML 2 (outerBoundaryIs) et GML 3 (exterior)
                if (name.equals("outerBoundaryIs") || name.equals("exterior")) {
                    shell = parseLinearRing(reader);
                } else if (name.equals("innerBoundaryIs") || name.equals("interior")) {
                    holes.add(parseLinearRing(reader));
                }
            }
            if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("Polygon")) break;
        }
        return gf.createPolygon(shell, holes.toArray(new LinearRing[0]));
    }

    private LinearRing parseLinearRing(XMLStreamReader reader) throws XMLStreamException {
        Coordinate[] coords = null;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = reader.getLocalName();
                if (name.equals("coordinates") || name.equals("posList")) {
                    // Utilisation de notre parseur robuste créé précédemment
                    coords = CoordinateParser.parse(reader.getElementText(), 2);
                }
            }
            if (event == XMLStreamConstants.END_ELEMENT && 
               (reader.getLocalName().contains("BoundaryIs") || 
                reader.getLocalName().equals("exterior") || 
                reader.getLocalName().equals("interior") ||
                reader.getLocalName().equals("LinearRing"))) break;
        }
        return gf.createLinearRing(coords);
    }

    private Geometry parseMultiGeometry(XMLStreamReader reader, String type) throws XMLStreamException {
        List<Geometry> parts = new ArrayList<>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                // Récursion : un MultiPolygon contient des Polygons
                Geometry g = parse(reader);
                if (g != null) parts.add(g);
            }
            if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(type)) break;
        }
        return gf.buildGeometry(parts);
    }

    private Point parsePoint(XMLStreamReader reader) throws XMLStreamException {
        Coordinate[] coords = null;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals("coordinates") || reader.getLocalName().equals("pos")) {
                    coords = CoordinateParser.parse(reader.getElementText(), 2);
                }
            }
            if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("Point")) break;
        }
        return (coords != null && coords.length > 0) ? gf.createPoint(coords[0]) : null;
    }

    private LineString parseLineString(XMLStreamReader reader) throws XMLStreamException {
        Coordinate[] coords = null;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals("coordinates") || reader.getLocalName().equals("posList")) {
                    coords = CoordinateParser.parse(reader.getElementText(), 2);
                }
            }
            if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("LineString")) break;
        }
        return gf.createLineString(coords);
    }
}
