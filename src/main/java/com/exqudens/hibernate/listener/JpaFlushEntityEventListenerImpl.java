package com.exqudens.hibernate.listener;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.jpa.event.internal.core.JpaFlushEntityEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaFlushEntityEventListenerImpl extends JpaFlushEntityEventListener {

    private static final long serialVersionUID = -3628174676756352536L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaFlushEntityEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaFlushEntityEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onFlushEntity(FlushEntityEvent event) throws HibernateException {
        LOG.trace("");
        super.onFlushEntity(event);
    }

}
