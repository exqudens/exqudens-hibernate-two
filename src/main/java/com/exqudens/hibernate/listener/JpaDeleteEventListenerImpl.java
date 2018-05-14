package com.exqudens.hibernate.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.jpa.event.internal.core.JpaDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaDeleteEventListenerImpl extends JpaDeleteEventListener implements DeleteEventListener,
SelfCacheEventListener {

    private static final Logger LOG;
    private static final long   serialVersionUID;

    private final Map<Integer, List<Object>> cache;

    static {
        LOG = LoggerFactory.getLogger(JpaDeleteEventListenerImpl.class);
        LOG.trace("");
        serialVersionUID = -8236292841718033661L;
    }

    public JpaDeleteEventListenerImpl() {
        super();
        LOG.trace("");
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public List<Object> remove(EventSource eventSource) {
        return cache.remove(System.identityHashCode(eventSource));
    }

    @Override
    protected boolean invokeDeleteLifecycle(EventSource session, Object entity, EntityPersister persister) {
        Integer sessionIdentityHashCode = System.identityHashCode(session);
        cache.putIfAbsent(sessionIdentityHashCode, new LinkedList<>());
        cache.get(sessionIdentityHashCode).add(entity);
        return super.invokeDeleteLifecycle(session, entity, persister);
    }

}
