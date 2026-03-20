package org.exist.indexing.spatial;

import org.locationtech.jts.geom.Geometry;
import java.util.Map;

/**
 * Interface définissant le contrat de stockage spatial.
 * Gère l'indexation granulaire (partie par partie).
 */
public interface SpatialStore {

    /**
     * Initialisation du store avec les paramètres du collection.xconf
     */
    void init(Map<String, String> params) throws Exception;

    /**
     * Indexation d'une partie de géométrie.
     * @param nodeId Identifiant interne eXist du document/nœud
     * @param part La géométrie JTS d'une partie (ex: un polygone d'un MultiPolygon)
     * @param partIndex L'index de la partie (0..N)
     * @param srsId Le code SRS (ex: EPSG:4326), peut être null si défaut
     * @param txnId Identifiant de la transaction eXist
     */
    void addGeometryPart(String nodeId, Geometry part, int partIndex, String srsId, long txnId) throws Exception;

    /**
     * Commit des données en fin de transaction
     */
    void commit(long txnId) throws Exception;

    /**
     * Rollback en cas d'erreur (Logique transactionnelle absolue)
     */
    void rollback(long txnId) throws Exception;

    /**
     * Fermeture propre des ressources (HSQLDB, etc.)
     */
    void shutdown() throws Exception;
}
