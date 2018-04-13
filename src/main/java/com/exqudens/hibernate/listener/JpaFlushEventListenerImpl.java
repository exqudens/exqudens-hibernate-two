package com.exqudens.hibernate.listener;

import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.jpa.event.internal.core.JpaFlushEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaFlushEventListenerImpl extends JpaFlushEventListener {

    private static final long serialVersionUID = -6001914559746251140L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaFlushEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaFlushEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onFlush(FlushEvent event) throws HibernateException {
        LOG.trace("");
        System.out.println("-----------------------------------------------------------------------------");
        for (Entry<Object, EntityEntry> entry : event.getSession().getPersistenceContext().reentrantSafeEntityEntries()) {
            EntityEntry entityEntry = entry.getValue();
            Object entity = entry.getKey();
            System.out.println(entityEntry + " - " + entity);
        }
        System.out.println("-----------------------------------------------------------------------------");
        super.onFlush(event);
    }

}
