package com.exqudens.hibernate.listener;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.PersistentObjectException;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.jpa.event.internal.core.JpaPersistOnFlushEventListener;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaPersistOnFlushEventListenerImpl extends JpaPersistOnFlushEventListener {

    private static final long serialVersionUID = 3437493572395316229L;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(JpaPersistOnFlushEventListenerImpl.class);
        LOG.trace("");
    }

    public JpaPersistOnFlushEventListenerImpl() {
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
        SessionImplementor source = event.getSession();
        Object object = event.getObject();

        Object entity;
        if ( object instanceof HibernateProxy ) {
            LazyInitializer li = ( (HibernateProxy) object ).getHibernateLazyInitializer();
            if ( li.isUninitialized() ) {
                if ( li.getSession() == source ) {
                    return; //NOTE EARLY EXIT!
                }
                else {
                    throw new PersistentObjectException( "uninitialized proxy passed to persist()" );
                }
            }
            entity = li.getImplementation();
        }
        else {
            entity = object;
        }
        String entityName;
        if ( event.getEntityName() != null ) {
            entityName = event.getEntityName();
        }
        else {
            entityName = source.bestGuessEntityName( entity );
            event.setEntityName( entityName );
        }
        EntityEntry entityEntry = source.getPersistenceContext().getEntry( event.getObject() );
        EntityState entityState = getEntityState( entity, entityName, entityEntry, source );
        LOG.info(entity.getClass().getName() + ": " + entityState.name());
        super.onPersist(event, createCache);
    }

}
