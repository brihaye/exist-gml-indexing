package org.exist.indexing.spatial;

import org.locationtech.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseur robuste capable de lire des coordonnées GML 2 (séparées par des virgules)
 * et GML 3 (posList séparées par des espaces).
 */
public class CoordinateParser {

    /**
     * @param text Le contenu textuel de l'élément coordinates ou posList
     * @param dimension 2 pour (X,Y) ou 3 pour (X,Y,Z)
     */
    public static Coordinate[] parse(String text, int dimension) {
        if (text == null || text.trim().isEmpty()) {
            return new Coordinate[0];
        }

        String clean = text.trim();
        // Détection du format : si présence de virgule -> GML 2 (X,Y X,Y)
        // Sinon -> GML 3 (X Y X Y)
        String[] tokens;
        if (clean.contains(",")) {
            // Format GML 2 : on sépare par les espaces, puis on traite la virgule
            tokens = clean.split("[\\s,]+");
        } else {
            // Format GML 3 : on sépare par n'importe quel espace (blanc, tab, saut de ligne)
            tokens = clean.split("\\s+");
        }

        List<Coordinate> coords = new ArrayList<>();
        for (int i = 0; i < tokens.length; i += dimension) {
            if (i + 1 < tokens.length) {
                try {
                    double x = Double.parseDouble(tokens[i]);
                    double y = Double.parseDouble(tokens[i+1]);
                    double z = (dimension == 3 && i + 2 < tokens.length) 
                               ? Double.parseDouble(tokens[i+2]) : Double.NaN;
                    coords.add(new Coordinate(x, y, z));
                } catch (NumberFormatException e) {
                    // Logique de résilience : on ignore un token mal formé
                }
            }
        }
        return coords.toArray(new Coordinate[0]);
    }
}
