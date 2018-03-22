package com.exqudens.hibernate.listener;

import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.jpa.event.internal.core.JpaDeleteEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaDeleteEventListenerImpl extends JpaDeleteEventListener {

    private static final long serialVersionUID = -8236292841718033661L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaDeleteEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaDeleteEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onDelete(DeleteEvent event) throws HibernateException {
        LOG.trace("");
        super.onDelete(event);
    }

    @Override
    public void onDelete(DeleteEvent event, @SuppressWarnings("rawtypes") Set transientEntities) throws HibernateException {
        LOG.trace("");
        super.onDelete(event, transientEntities);
    }

}
