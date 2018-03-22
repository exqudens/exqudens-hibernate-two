package com.exqudens.hibernate.listener;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.jpa.event.internal.core.JpaMergeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaMergeEventListenerImpl extends JpaMergeEventListener {

    private static final long serialVersionUID = 2019894302500268842L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaMergeEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaMergeEventListenerImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        LOG.trace("");
        super.onMerge(event);
    }

    @Override
    public void onMerge(MergeEvent event, @SuppressWarnings("rawtypes") Map copiedAlready) throws HibernateException {
        LOG.trace("");
        super.onMerge(event, copiedAlready);
    }

}
