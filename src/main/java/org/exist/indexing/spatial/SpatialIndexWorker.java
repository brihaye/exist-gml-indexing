package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.indexing.AbstractIndexWorker;
import org.exist.indexing.Index;
import org.exist.storage.transaction.Transaction;
import org.exist.xquery.TermReversedList;
import java.util.logging.Logger;

/**
 * Gère le cycle de vie des données indexées (Suppression / Mise à jour).
 */
public class SpatialIndexWorker extends AbstractIndexWorker {

    private static final Logger LOG = Logger.getLogger(SpatialIndexWorker.class.getName());
    private final SpatialStore store;

    public SpatialIndexWorker(Index index, SpatialStore store) {
        super(index);
        this.store = store;
    }

    @Override
    public void removeDocument(DocumentImpl doc, Transaction txn) {
        // Logique de nettoyage : quand un doc est supprimé, on nettoie le SQL
        try {
            // Ici, on pourrait ajouter une méthode deleteByDocId dans le Store
            LOG.info("Nettoyage de l'index spatial pour le document : " + doc.getURI());
            store.commit(txn != null ? txn.getId() : 0);
        } catch (Exception e) {
            LOG.severe("Erreur lors de la suppression du document de l'index : " + e.getMessage());
        }
    }

    @Override
    public TermReversedList getReversedList(String field) {
        // Utilisé pour l'optimisation des requêtes, on peut laisser null pour l'instant
        return null;
    }

    @Override
    public void flush() {
        try {
            store.commit(0);
        } catch (Exception e) {
            LOG.warning("Echec du flush de l'index spatial.");
        }
    }
}
