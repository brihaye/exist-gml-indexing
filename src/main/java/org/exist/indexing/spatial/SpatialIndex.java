package org.exist.indexing.spatial;

import org.exist.indexing.AbstractIndex;
import org.exist.indexing.IndexWorker;
import org.exist.indexing.IndexController;
import java.util.Map;

public class SpatialIndex extends AbstractIndex {

    // On utilise AbstractSpatialStore comme base commune
    private AbstractSpatialStore store;

	@Override
	public void configure(org.exist.storage.BrokerPool pool, java.nio.file.Path path, org.w3c.dom.Element config) {
	    // On laisse vide pour l'instant pour tester la compilation
	}
	
	@Override
	public void checkIndex(org.exist.storage.DBBroker broker) {
	    // Cette méthode est devenue obligatoire (abstract)
	}

    @Override
    public IndexWorker getWorker(IndexController controller) {
        return new SpatialIndexWorker(this, controller);
    }

    // UNE SEULE FOIS cette méthode pour que le Worker puisse y accéder
    public AbstractSpatialStore getStore() {
        return this.store;
    }

    @Override
    public void close() {
        if (store != null) {
            store.flush();
        }
    }
}
