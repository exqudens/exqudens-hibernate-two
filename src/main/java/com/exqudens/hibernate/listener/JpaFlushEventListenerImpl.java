package com.exqudens.hibernate.listener;

import org.hibernate.HibernateException;
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
        super.onFlush(event);
    }

}
