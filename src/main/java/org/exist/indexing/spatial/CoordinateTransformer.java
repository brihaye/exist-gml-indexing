package org.exist.indexing.spatial;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.proj4j.*;

import java.util.concurrent.ConcurrentHashMap;

public class CoordinateTransformer {

    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
    
    // Cache pour éviter de reconstruire les CRS à chaque appel
    private static final ConcurrentHashMap<String, CoordinateTransform> transformCache = new ConcurrentHashMap<>();

    /**
     * Transforme une géométrie JTS vers un SRS cible (généralement WGS84)
     */
    public static Geometry transform(Geometry geom, String sourceSrsName, String targetSrsName) {
        if (sourceSrsName == null || sourceSrsName.equalsIgnoreCase(targetSrsName)) {
            return geom.copy();
        }

        CoordinateTransform transform = getCachedTransform(sourceSrsName, targetSrsName);
        Geometry transformedGeom = geom.copy();
        
        // Application de la transformation sur chaque coordonnée
        transformedGeom.apply((org.locationtech.jts.geom.CoordinateFilter) coord -> {
            ProjCoordinate src = new ProjCoordinate(coord.x, coord.y);
            ProjCoordinate dst = new ProjCoordinate();
            transform.transform(src, dst);
            coord.x = dst.x;
            coord.y = dst.y;
        });

        return transformedGeom;
    }

    private static CoordinateTransform getCachedTransform(String source, String target) {
        String key = source + "-->" + target;
        return transformCache.computeIfAbsent(key, k -> {
            CoordinateReferenceSystem srcCRS = crsFactory.createFromName(source); // ex: "EPSG:2154"
            CoordinateReferenceSystem tgtCRS = crsFactory.createFromName(target); // ex: "EPSG:4326"
            return ctFactory.createTransform(srcCRS, tgtCRS);
        });
    }
}
