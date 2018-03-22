package com.exqudens.hibernate.listener;

import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.jpa.event.internal.core.JpaSaveOrUpdateEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaSaveOrUpdateEventListenerImpl extends JpaSaveOrUpdateEventListener {

    private static final long serialVersionUID = 84324162440452843L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaSaveOrUpdateEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaSaveOrUpdateEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event) {
        LOG.trace("");
        super.onSaveOrUpdate(event);
    }

}
