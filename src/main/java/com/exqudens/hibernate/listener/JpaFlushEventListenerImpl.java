package com.exqudens.hibernate.listener;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.jpa.event.internal.core.JpaFlushEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exqudens.hibernate.persister.Persister;
import com.exqudens.hibernate.util.SortUtils;

public class JpaFlushEventListenerImpl extends JpaFlushEventListener implements FlushEventListener {

    private static final Logger LOG;
    private static final long   serialVersionUID;

    static {
        LOG = LoggerFactory.getLogger(JpaFlushEventListenerImpl.class);
        LOG.trace("");
        serialVersionUID = -6001914559746251140L;
    }

    public JpaFlushEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onFlush(FlushEvent event) throws HibernateException {
        LOG.trace("");

        event.getSession().getActionQueue().clear();

        List<Object> insertCache = getAndClearCache(event, EventType.PERSIST);
        List<Object> updateCache = getAndClearCache(event, EventType.MERGE);
        List<Object> deleteCache = getAndClearCache(event, EventType.DELETE);

        List<List<Object>> insertCacheSorted = SortUtils.sort(insertCache, event.getSession());
        for (List<Object> batch : insertCacheSorted) {
            EntityPersister ep = event.getSession().getEntityPersister(null, batch.iterator().next());
            if (ep.getIdentifierType().isComponentType()) {
                Persister step = Persister.class.cast(ep);
                step.insert(batch, event.getSession());
            } else {
                Persister step = Persister.class.cast(ep);
                List<Entry<Serializable, Object>> insertEntries = step.insertIdentity(batch, event.getSession());
                for (int i = 0; i < batch.size(); i++) {
                    EntityKey entityKey = event.getSession().getPersistenceContext().getEntry(batch.get(i))
                    .getEntityKey();
                    event.getSession().getPersistenceContext().replaceDelayedEntityIdentityInsertKeys(
                        entityKey,
                        insertEntries.get(i).getKey()
                    );
                }
                for (Entry<Serializable, Object> insertEntry : insertEntries) {
                    if (insertEntry.getValue() != null) {
                        updateCache.add(insertEntry.getValue());
                    }
                }
            }
            System.out.println("insert DONE: " + batch);
        }

        List<List<Object>> updateCacheSorted = SortUtils.sort(updateCache, event.getSession());
        for (List<Object> batch : updateCacheSorted) {
            EntityPersister ep = event.getSession().getEntityPersister(null, batch.iterator().next());
            if (ep.getIdentifierType().isComponentType()) {
                throw new UnsupportedOperationException();
            } else {
                Persister step = Persister.class.cast(ep);
                step.update(batch, event.getSession());
            }
            System.out.println("update DONE: " + batch);
        }

        List<List<Object>> deleteCacheSorted = SortUtils.sort(deleteCache, event.getSession());
        for (int i = deleteCacheSorted.size() - 1; i >= 0; i--) {
            List<Object> batch = deleteCacheSorted.get(i);
            EntityPersister ep = event.getSession().getEntityPersister(null, batch.iterator().next());
            if (ep.getIdentifierType().isComponentType()) {
                throw new UnsupportedOperationException();
            } else {
                Persister step = Persister.class.cast(ep);
                step.delete(batch, event.getSession());
            }
            System.out.println("delete DONE: " + batch);
        }

        //super.onFlush(event);
    }

    private <T> List<Object> getAndClearCache(FlushEvent event, EventType<T> type) {
        EventSource session = event.getSession();

        EventListenerGroup<T> eventListenerGroup = session.getFactory().getServiceRegistry().getService(
            EventListenerRegistry.class
        ).getEventListenerGroup(type);

        for (T listener : eventListenerGroup.listeners()) {

            if (SelfCacheEventListener.class.isInstance(listener)) {

                List<Object> cache = SelfCacheEventListener.class.cast(listener).remove(event.getSession());

                if (cache != null) {
                    SelfCacheEventListener.class.cast(listener).remove(event.getSession());
                    return cache;
                } else {
                    break;
                }
            }
        }
        return new LinkedList<>();
    }

}
