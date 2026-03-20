package org.exist.indexing.spatial;

import org.locationtech.proj4j.*;
import org.locationtech.jts.geom.Coordinate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Gère les transformations de coordonnées et le calcul de précision (Padding).
 * Utilise un cache pour optimiser les performances sur les zones répétitives.
 */
public class ProjectionService {

    private final CRSFactory crsFactory = new CRSFactory();
    private final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
    private final Map<String, Double> precisionCache = new ConcurrentHashMap<>();
    
    private String defaultSrs = "EPSG:4326"; // WGS84 par défaut

    /**
     * Calcule le padding nécessaire pour une coordonnée et un SRS donnés.
     * Implémente la mémorisation pour éviter les calculs Proj4J coûteux.
     */
    public double getPadding(Coordinate coord, String srsId) {
        String effectiveSrs = (srsId != null) ? srsId : defaultSrs;
        
        // Clé de cache basée sur le SRS et une grille de 0.1 degré (~11km)
        String cacheKey = effectiveSrs + "_" + Math.round(coord.y * 10.0) + "_" + Math.round(coord.x * 10.0);
        
        return precisionCache.computeIfAbsent(cacheKey, key -> calculateDynamicDelta(coord, effectiveSrs));
    }

    private double calculateDynamicDelta(Coordinate coord, String srsCode) {
        try {
            CoordinateReferenceSystem srcCrs = crsFactory.createFromName(srsCode);
            CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");
            CoordinateTransform transform = ctFactory.createTransform(srcCrs, wgs84);

            // Simulation d'un micro-segment de 100m pour mesurer la distorsion locale
            ProjCoordinate p1 = new ProjCoordinate(coord.x, coord.y);
            ProjCoordinate p2 = new ProjCoordinate(coord.x + 100.0, coord.y);
            ProjCoordinate out1 = new ProjCoordinate();
            ProjCoordinate out2 = new ProjCoordinate();

            transform.transform(p1, out1);
            transform.transform(p2, out2);

            // Calcul de l'erreur d'échelle (simplifié pour l'exemple)
            double distCarto = 100.0;
            double distGeo = Math.sqrt(Math.pow(out1.x - out2.x, 2) + Math.pow(out1.y - out2.y, 2)) * 111139.0;
            
            double scaleError = Math.abs(distCarto - distGeo);
            return scaleError + 2.0; // +2m de sécurité pour le pivot de Datum
            
        } catch (Exception e) {
            return 25.0; // Fallback sur votre valeur "à la louche" en cas d'erreur
        }
    }

    public void setDefaultSrs(String srs) {
        this.defaultSrs = srs;
    }
}
