package com.exqudens.hibernate.listener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.jpa.event.internal.core.JpaMergeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaMergeEventListenerImpl extends JpaMergeEventListener implements MergeEventListener,
SelfCacheEventListener {

    private static final Logger LOG;
    private static final long   serialVersionUID;

    private final Map<Integer, List<Object>> cache;

    static {
        LOG = LoggerFactory.getLogger(JpaMergeEventListenerImpl.class);
        LOG.trace("");
        serialVersionUID = 2019894302500268842L;
    }

    public JpaMergeEventListenerImpl() {
        super();
        LOG.trace("");
        cache = new ConcurrentHashMap<>();
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

    @Override
    public List<Object> remove(EventSource eventSource) {
        return cache.remove(System.identityHashCode(eventSource));
    }

}
