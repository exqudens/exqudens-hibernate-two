package com.exqudens.hibernate.listener;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.jpa.event.internal.core.JpaPersistOnFlushEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaPersistOnFlushEventListenerImpl extends JpaPersistOnFlushEventListener {

    private static final long serialVersionUID = 3437493572395316229L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaPersistOnFlushEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaPersistOnFlushEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onPersist(PersistEvent event) throws HibernateException {
        LOG.trace("");
        super.onPersist(event);
    }

    @Override
    public void onPersist(PersistEvent event, @SuppressWarnings("rawtypes") Map createCache) throws HibernateException {
        LOG.trace("");
        super.onPersist(event, createCache);
    }

}
