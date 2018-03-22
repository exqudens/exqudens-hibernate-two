package com.exqudens.hibernate.listener;

import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.jpa.event.internal.core.JpaSaveEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaSaveEventListenerImpl extends JpaSaveEventListener {

    private static final long serialVersionUID = 8579346340911233438L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaSaveEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaSaveEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) {
        LOG.trace("");
        super.onSaveOrUpdate(event);
    }

}
