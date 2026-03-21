package org.exist.indexing.spatial;

import org.exist.indexing.AbstractIndexWorker;
import org.exist.indexing.IndexController;
import org.exist.indexing.StreamListener;
import org.exist.storage.DBBroker;
import org.w3c.dom.NodeList;
import java.util.Map;

/**
 * En étendant AbstractIndexWorker, on hérite des implémentations par défaut
 * de getDocument(), setDocument(), getMode(), etc.
 */
public class SpatialIndexWorker extends AbstractIndexWorker {

    private final SpatialIndex index;
    private final DBBroker broker;

    public SpatialIndexWorker(SpatialIndex index, DBBroker broker) {
        this.index = index;
        this.broker = broker;
    }

    @Override
    public String getIndexId() {
        return "http://exist-db.org/indexing/spatial";
    }

    @Override
    public void configure(IndexController controller, NodeList configNodes, Map<String, String> params) {
        // On récupère ici les paramètres du conf.xml si besoin
    }

    @Override
    public StreamListener getListener() {
        // C'est ici qu'on branchera ton SpatialStreamListener de 2007
        return null; 
    }

    @Override
    public void flush() {
        if (index.getStore() != null) {
            index.getStore().flush();
        }
    }

    // remove() et les autres méthodes sont déjà gérées par AbstractIndexWorker
    // On ne les réécrit que si on a une logique spécifique.
}
