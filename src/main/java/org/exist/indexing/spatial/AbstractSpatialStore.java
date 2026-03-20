package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.NodeProxy;
import org.exist.storage.txn.Txn;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.gml2.GMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Store spatial de base pour eXist-db 6.x.
 * Gère la lecture GML et le cycle de vie des documents indexés.
 */
public abstract class AbstractSpatialStore {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpatialStore.class);
    
    // Le lecteur GML de JTS (LocationTech)
    protected final GMLReader gmlReader = new GMLReader();
    
    // Cache en mémoire pour les tests et la performance immédiate
    // Clé : ID unique du nœud dans eXist
    protected final Map<Long, Geometry> internalCache = new ConcurrentHashMap<>();

    /**
     * Ajoute une géométrie GML à l'index.
     * @param txn La transaction en cours (eXist 6.x)
     * @param node Le nœud XML contenant la géométrie
     * @param gmlString La chaîne GML brute
     */
    public void addGeometry(Txn txn, NodeProxy node, String gmlString) {
        try {
            // Nettoyage rapide de la chaîne si nécessaire
            Geometry geometry = gmlReader.read(gmlString, null);
            
            if (geometry != null) {
                // On utilise le "Coordinate" du NodeId comme clé unique
                long nodeId = node.getNodeId().getCoordinate();
                internalCache.put(nodeId, geometry);
                
                // Ici, on appellerait la méthode persistante (ex: SQL)
                saveToPersistentStore(txn, nodeId, geometry);
                
                LOG.debug("Géométrie indexée pour le nœud {}", nodeId);
            }
        } catch (Exception e) {
            LOG.error("Erreur lors de la lecture GML pour le nœud " + node.getNodeId(), e);
        }
    }

    /**
     * Supprime toutes les entrées spatiales liées à un document.
     */
    public void removeDocument(Txn txn, DocumentImpl document) {
        LOG.info("Suppression de l'index spatial pour : {}", document.getURI());
        // Logique de nettoyage (à implémenter dans la classe fille)
        removeFromPersistentStore(txn, document);
        
        // Nettoyage du cache (simplifié)
        internalCache.clear(); 
    }

    /**
     * Force l'écriture des données sur le disque.
     */
    public abstract void flush();

    // Méthodes à implémenter par ton stockage réel (ex: SpatialStoreImpl)
    protected abstract void saveToPersistentStore(Txn txn, long nodeId, Geometry geom);
    protected abstract void removeFromPersistentStore(Txn txn, DocumentImpl doc);

    /**
     * Utile pour les tests JUnit : vérifie si un point est contenu dans une géométrie indexée.
     */
    public boolean contains(long nodeId, Geometry searchGeom) {
        Geometry indexedGeom = internalCache.get(nodeId);
        return indexedGeom != null && indexedGeom.contains(searchGeom);
    }
}
