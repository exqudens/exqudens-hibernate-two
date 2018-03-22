package com.exqudens.hibernate.listener;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.jpa.event.internal.core.JpaAutoFlushEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaAutoFlushEventListenerImpl extends JpaAutoFlushEventListener {

    private static final long serialVersionUID = 5326999394367686252L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaAutoFlushEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaAutoFlushEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onAutoFlush(AutoFlushEvent event) throws HibernateException {
        LOG.trace("");
        super.onAutoFlush(event);
    }

}
