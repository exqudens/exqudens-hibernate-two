package com.exqudens.hibernate.util;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.persister.internal.PersisterClassResolverInitiator;
import org.hibernate.service.StandardServiceInitiators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exqudens.hibernate.multitenancy.MultiTenantConnectionProviderImpl;

public class PersistenceUnitInfoUtils {

    public static String HIBERNATE_PERSISTENCE_PROVIDER_CLASS_NAME;
    public static String ECLIPSELINK_PERSISTENCE_PROVIDER_CLASS_NAME;
    public static String OPENJPA_PERSISTENCE_PROVIDER_CLASS_NAME;

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(PersistenceUnitInfoUtils.class);
        LOG.trace("");
        HIBERNATE_PERSISTENCE_PROVIDER_CLASS_NAME = "org.hibernate.jpa.HibernatePersistenceProvider";
        ECLIPSELINK_PERSISTENCE_PROVIDER_CLASS_NAME = "org.eclipse.persistence.jpa.PersistenceProvider";
        OPENJPA_PERSISTENCE_PROVIDER_CLASS_NAME = "org.apache.openjpa.persistence.PersistenceProviderImpl";
    }

    public static PersistenceUnitInfo createHibernatePersistenceUnitInfo(
        String persistenceUnitName,
        Map<String, DataSource> dataSourceMap,
        Map<String, Object> properties,
        Class<?>... classes
    ) {
        LOG.trace("");
        try {
            Object integratorProviderClassName = properties.get(EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER);
            if (integratorProviderClassName != null) {
                Class<?> integratorProviderClass = Class.forName(integratorProviderClassName.toString());
                IntegratorProvider integratorProvider = IntegratorProvider.class.cast(
                    integratorProviderClass.newInstance()
                );
                properties.put(EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER, integratorProvider);
            }

            Object multiTenantConnectionProviderClassName = properties.get(
                AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER
            );
            if (multiTenantConnectionProviderClassName != null) {
                if (
                    MultiTenantConnectionProviderImpl.class.getName().equals(
                        multiTenantConnectionProviderClassName.toString()
                    )
                ) {
                    MultiTenantConnectionProviderImpl multiTenantConnectionProviderImpl = new MultiTenantConnectionProviderImpl(
                        dataSourceMap
                    );
                    properties.put(
                        AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER,
                        multiTenantConnectionProviderImpl
                    );
                    properties.put(
                        AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                        multiTenantConnectionProviderImpl.getCurrentTenantIdentifierResolver()
                    );
                }
            }

            StandardServiceInitiators.LIST = StandardServiceInitiators.LIST.stream().map(
                s -> s instanceof PersisterClassResolverInitiator
                ? com.exqudens.hibernate.persister.PersisterClassResolverInitiatorImpl.INSTANCE
                : s
            ).collect(Collectors.toList());

            return createPersistenceUnitInfo(
                persistenceUnitName,
                HIBERNATE_PERSISTENCE_PROVIDER_CLASS_NAME,
                null,
                null,
                PersistenceUnitTransactionType.RESOURCE_LOCAL,
                properties,
                classes
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PersistenceUnitInfo createPersistenceUnitInfo(
        String persistenceUnitName,
        String persistenceProviderClassName,
        DataSource nonJtaDataSource,
        DataSource jtaDataSource,
        PersistenceUnitTransactionType persistenceUnitTransactionType,
        Map<String, ?> properties,
        Class<?>... classes
    ) {
        LOG.trace("");
        return new PersistenceUnitInfo() {

            @Override
            public String getPersistenceUnitName() {
                return persistenceUnitName;
            }

            @Override
            public String getPersistenceProviderClassName() {
                return persistenceProviderClassName;
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return nonJtaDataSource;
            }

            @Override
            public DataSource getJtaDataSource() {
                return jtaDataSource;
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return persistenceUnitTransactionType;
            }

            @Override
            public Properties getProperties() {
                Properties p = new Properties();
                p.putAll(properties);
                return p;
            }

            @Override
            public List<String> getManagedClassNames() {
                List<String> list = Arrays.asList(classes).stream().map(Class::getName).collect(Collectors.toList());
                return list;
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return true;
            }

            @Override
            public List<URL> getJarFileUrls() {
                List<URL> list;
                list = Collections.emptyList();
                return list;
            }

            @Override
            public List<String> getMappingFileNames() {
                return Collections.emptyList();
            }

            @Override
            public ValidationMode getValidationMode() {
                return null;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return null;
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return null;
            }

            @Override
            public URL getPersistenceUnitRootUrl() {
                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public ClassLoader getNewTempClassLoader() {
                return null;
            }

            @Override
            public void addTransformer(ClassTransformer transformer) {
            }

        };
    }

    private PersistenceUnitInfoUtils() {
        super();
        LOG.trace("");
    }

}
