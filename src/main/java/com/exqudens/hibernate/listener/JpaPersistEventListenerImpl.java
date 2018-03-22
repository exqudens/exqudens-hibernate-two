package com.exqudens.hibernate.listener;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.jpa.event.internal.core.JpaPersistEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaPersistEventListenerImpl extends JpaPersistEventListener {

    private static final long serialVersionUID = 3518832665689448139L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaPersistEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaPersistEventListenerImpl() {
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
