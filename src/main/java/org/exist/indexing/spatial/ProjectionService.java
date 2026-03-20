package org.exist.indexing.spatial;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de projection haute performance avec cache pour eXist-db 6.x
 */
public class ProjectionService {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectionService.class);

    private final CRSFactory crsFactory = new CRSFactory();
    private final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
    
    // Cache pour éviter de recalculer les transformations à chaque appel
    private final ConcurrentHashMap<String, CoordinateTransform> transformCache = new ConcurrentHashMap<>();

    /**
     * Transforme une coordonnée d'un SRS à un autre.
     */
    public ProjCoordinate transform(double x, double y, String fromSRS, String toSRS) {
        try {
            String cacheKey = fromSRS + "->" + toSRS;
            CoordinateTransform transform = transformCache.computeIfAbsent(cacheKey, key -> {
                CoordinateReferenceSystem src = crsFactory.createFromName(fromSRS);
                CoordinateReferenceSystem dest = crsFactory.createFromName(toSRS);
                return ctFactory.createTransform(src, dest);
            });

            ProjCoordinate srcCoord = new ProjCoordinate(x, y);
            ProjCoordinate destCoord = new ProjCoordinate();
            transform.transform(srcCoord, destCoord);
            
            return destCoord;
        } catch (Exception e) {
            LOG.error("Erreur de projection de {} vers {} pour le point [{},{}]", fromSRS, toSRS, x, y, e);
            return new ProjCoordinate(x, y); // Retourne l'original en cas d'échec
        }
    }

    /**
     * Vérifie si un SRS est supporté.
     */
    public boolean isSupported(String srsName) {
        try {
            return crsFactory.createFromName(srsName) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
