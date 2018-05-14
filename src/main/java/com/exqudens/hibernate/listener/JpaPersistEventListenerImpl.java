package com.exqudens.hibernate.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.jpa.event.internal.core.JpaPersistEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaPersistEventListenerImpl extends JpaPersistEventListener implements PersistEventListener,
SelfCacheEventListener {

    private static final Logger LOG;
    private static final long   serialVersionUID;

    private final Map<Integer, List<Object>> cache;

    static {
        LOG = LoggerFactory.getLogger(JpaPersistEventListenerImpl.class);
        LOG.trace("");
        serialVersionUID = 3518832665689448139L;
    }

    public JpaPersistEventListenerImpl() {
        super();
        LOG.trace("");
        cache = new ConcurrentHashMap<>();
    }

    @Override
    protected void entityIsTransient(PersistEvent event, @SuppressWarnings("rawtypes") Map createCache) {
        LOG.trace("");
        Integer sessionIdentityHashCode = System.identityHashCode(event.getSession());
        cache.putIfAbsent(sessionIdentityHashCode, new LinkedList<>());
        cache.get(sessionIdentityHashCode).add(event.getObject());
        super.entityIsTransient(event, createCache);
    }

    @Override
    public List<Object> remove(EventSource eventSource) {
        return cache.remove(System.identityHashCode(eventSource));
    }

}
