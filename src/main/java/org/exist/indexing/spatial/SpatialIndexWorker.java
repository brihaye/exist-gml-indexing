package org.exist.indexing.spatial;

import org.exist.dom.persistent.DocumentImpl;
import org.exist.dom.persistent.DocumentSet;
import org.exist.dom.persistent.NodeSet;
import org.exist.dom.persistent.NodeProxy;
import org.exist.dom.persistent.IStoredNode;
import org.exist.indexing.IndexWorker;
import org.exist.indexing.MatchListener;
import org.exist.indexing.StreamListener;
import org.exist.storage.DBBroker;
import org.exist.storage.NodePath;
import org.exist.storage.txn.Txn;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.QueryRewriter;
import org.exist.util.Occurrences;
import org.exist.collections.Collection;
import java.util.Map;

public class SpatialIndexWorker implements IndexWorker {

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

    // Si getMode() est demandé mais IndexMode est introuvable, 
    // essayons sans l'import ou avec le type int par défaut si c'est une vieille interface.
    // Mais ici, l'erreur disait qu'il manquait getDocument() !

    @Override
    public DocumentImpl getDocument() {
        return null;
    }

    @Override
    public boolean checkIndex(DBBroker broker) {
        return true;
    }

    @Override
    public QueryRewriter getQueryRewriter(XQueryContext context) {
        return null;
    }

    @Override
    public Occurrences[] scanIndex(XQueryContext context, DocumentSet docs, NodeSet nodes, Map<?, ?> params) {
        return null;
    }

    @Override
    public void removeCollection(Collection collection, DBBroker broker, boolean delete) {
    }

    @Override
    public MatchListener getMatchListener(DBBroker broker, NodeProxy node) {
        return null;
    }

    @Override
    public StreamListener getListener() {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IStoredNode getReindexRoot(IStoredNode node, NodePath path, boolean includeChildren, boolean includeSelf) {
        return node;
    }

    @Override
    public void flush() {
        if (index.getStore() != null) {
            index.getStore().flush();
        }
    }

    // Tentative de signature alternative pour remove si la précédente échoue
    @Override
    public void remove(Txn transaction, DocumentImpl document) {
        if (index.getStore() != null) {
            index.getStore().removeDocument(transaction, document);
        }
    }
}
