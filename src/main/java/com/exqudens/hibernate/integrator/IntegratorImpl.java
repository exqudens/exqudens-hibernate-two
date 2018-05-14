package com.exqudens.hibernate.integrator;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.DuplicationStrategy;
import org.hibernate.event.service.spi.DuplicationStrategy.Action;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.event.internal.core.HibernateEntityManagerEventListener;
import org.hibernate.jpa.event.internal.jpa.CallbackBuilderLegacyImpl;
import org.hibernate.jpa.event.internal.jpa.CallbackRegistryImpl;
import org.hibernate.jpa.event.spi.jpa.CallbackBuilder;
import org.hibernate.jpa.event.spi.jpa.CallbackRegistryConsumer;
import org.hibernate.jpa.event.spi.jpa.ListenerFactory;
import org.hibernate.jpa.event.spi.jpa.ListenerFactoryBuilder;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exqudens.hibernate.listener.JpaDeleteEventListenerImpl;
import com.exqudens.hibernate.listener.JpaFlushEventListenerImpl;
import com.exqudens.hibernate.listener.JpaMergeEventListenerImpl;
import com.exqudens.hibernate.listener.JpaPersistEventListenerImpl;

/**
 * @author exqudens
 * @see org.hibernate.jpa.event.spi.JpaIntegrator
 */
public class IntegratorImpl implements Integrator {

    public static final IntegratorImpl INSTANCE;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(IntegratorImpl.class);
        INSTANCE = new IntegratorImpl();
    }

    private CallbackRegistryImpl callbackRegistry;
    private ListenerFactory      jpaListenerFactory;
    private CallbackBuilder      callbackBuilder;

    private IntegratorImpl() {
        super();
        LOG.trace("");
    }

    @Override
    public void integrate(
        Metadata metadata,
        SessionFactoryImplementor sessionFactory,
        SessionFactoryServiceRegistry serviceRegistry
    ) {
        LOG.trace("");
        EventListenerRegistry eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);

        eventListenerRegistry.addDuplicationStrategy(createDuplicationStrategy(Action.REPLACE_ORIGINAL));

        eventListenerRegistry.setListeners(EventType.PERSIST, new JpaPersistEventListenerImpl());
        eventListenerRegistry.setListeners(EventType.MERGE, new JpaMergeEventListenerImpl());
        eventListenerRegistry.setListeners(EventType.DELETE, new JpaDeleteEventListenerImpl());
        eventListenerRegistry.setListeners(EventType.FLUSH, new JpaFlushEventListenerImpl());
        //eventListenerRegistry.setListeners(EventType.AUTO_FLUSH, new JpaAutoFlushEventListenerImpl());
        //eventListenerRegistry.setListeners(EventType.FLUSH_ENTITY, new JpaFlushEntityEventListenerImpl());
        //eventListenerRegistry.setListeners(EventType.PERSIST_ONFLUSH, new JpaPersistOnFlushEventListenerImpl());
        //eventListenerRegistry.setListeners(EventType.SAVE, new JpaSaveEventListenerImpl());
        //eventListenerRegistry.setListeners(EventType.SAVE_UPDATE, new JpaSaveOrUpdateEventListenerImpl());

        ReflectionManager reflectionManager = MetadataImpl.class.cast(metadata).getMetadataBuildingOptions()
        .getReflectionManager();

        this.callbackRegistry = new CallbackRegistryImpl();
        this.jpaListenerFactory = ListenerFactoryBuilder.buildListenerFactory(
            sessionFactory.getSessionFactoryOptions()
        );
        this.callbackBuilder = new CallbackBuilderLegacyImpl(jpaListenerFactory, reflectionManager);
        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
            if (persistentClass.getClassName() == null) {
                // we can have non java class persisted by hibernate
                continue;
            }
            callbackBuilder.buildCallbacksForEntity(persistentClass.getClassName(), callbackRegistry);
        }

        for (EventType<?> eventType : EventType.values()) {
            final EventListenerGroup<?> eventListenerGroup = eventListenerRegistry.getEventListenerGroup(eventType);
            for (Object listener : eventListenerGroup.listeners()) {
                if (CallbackRegistryConsumer.class.isInstance(listener)) {
                    CallbackRegistryConsumer.class.cast(listener).injectCallbackRegistry(callbackRegistry);
                }
            }
        }
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        LOG.trace("");
        if (callbackRegistry != null) {
            callbackRegistry.release();
        }
        if (callbackBuilder != null) {
            callbackBuilder.release();
        }
        if (jpaListenerFactory != null) {
            jpaListenerFactory.release();
        }
    }

    private DuplicationStrategy createDuplicationStrategy(Action action) {
        return new DuplicationStrategy() {

            @Override
            public Action getAction() {
                return action;
            }

            @Override
            public boolean areMatch(Object listener, Object original) {
                return listener.getClass().equals(original.getClass()) && HibernateEntityManagerEventListener.class
                .isInstance(original);
            }
        };
    }

}
