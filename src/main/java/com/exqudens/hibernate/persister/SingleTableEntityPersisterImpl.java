package com.exqudens.hibernate.persister;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.entity.CascadeEntityLoader;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.entity.MultiLoadOptions;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.resource.jdbc.spi.PhysicalConnectionHandlingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exqudens.hibernate.multitenancy.MultiTenantConnectionProviderImpl;

public class SingleTableEntityPersisterImpl extends SingleTableEntityPersister implements PostInsertIdentityPersister {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(SingleTableEntityPersisterImpl.class);
    }

    private final boolean isMySQLDialect;

    public SingleTableEntityPersisterImpl(
            PersistentClass persistentClass,
            EntityRegionAccessStrategy cacheAccessStrategy,
            NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy,
            PersisterCreationContext creationContext
    ) throws HibernateException {
        super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, creationContext);
        LOG.trace("");
        isMySQLDialect = getFactory().getJdbcServices().getDialect() instanceof MySQLDialect;
        if (isMySQLDialect) {
            IntStream.range(0, updateResultCheckStyles.length)
            .forEach(i -> updateResultCheckStyles[i] = ExecuteUpdateResultCheckStyle.NONE);
        }
    }

    @Override
    public void insert(Serializable id, Object[] fields, Object object, SharedSessionContractImplementor session) {
        LOG.trace("");
        super.insert(id, fields, object, session);
    }

    @Override
    protected Serializable insert(Object[] fields, boolean[] notNull, String sql, Object object, SharedSessionContractImplementor session) throws HibernateException {
        LOG.trace("");
        return super.insert(fields, notNull, sql, object, session);
    }

    @Override
    protected UniqueEntityLoader getAppropriateLoader(LockOptions lockOptions, SharedSessionContractImplementor session) {
        UniqueEntityLoader loader = super.getAppropriateLoader(lockOptions, session);
        if (loader instanceof CascadeEntityLoader) {
            return (UniqueEntityLoader) getLoaders().get(LockMode.READ);
        }
        return loader;
    }

    @Override
    public Object load(Serializable id, Object optionalObject, LockMode lockMode, SharedSessionContractImplementor session) {
        LOG.trace("");
        preLoad(session);
        return super.load(id, optionalObject, lockMode, session);
    }

    @Override
    public Object load(Serializable id, Object optionalObject, LockOptions lockOptions, SharedSessionContractImplementor session) throws HibernateException {
        LOG.trace("");
        preLoad(session);
        return super.load(id, optionalObject, lockOptions, session);
    }

    @Override
    public Object loadByUniqueKey(String propertyName, Object uniqueKey, SharedSessionContractImplementor session) throws HibernateException {
        LOG.trace("");
        preLoad(session);
        return super.loadByUniqueKey(propertyName, uniqueKey, session);
    }

    @Override
    public Serializable loadEntityIdByNaturalId(Object[] naturalIdValues, LockOptions lockOptions, SharedSessionContractImplementor session) {
        LOG.trace("");
        preLoad(session);
        return super.loadEntityIdByNaturalId(naturalIdValues, lockOptions, session);
    }

    @Override
    public List<?> multiLoad(Serializable[] ids, SharedSessionContractImplementor session, MultiLoadOptions loadOptions) {
        LOG.trace("");
        preLoad(session);
        return super.multiLoad(ids, session, loadOptions);
    }

    @Override
    public void update(Serializable id, Object[] fields, int[] dirtyFields, boolean hasDirtyCollection, Object[] oldFields, Object oldVersion, Object object, Object rowId, SharedSessionContractImplementor session) throws HibernateException {
        LOG.trace("");
        super.update(id, fields, dirtyFields, hasDirtyCollection, oldFields, oldVersion, object, rowId, session);
    }

    @Override
    public void delete(Serializable id, Object version, Object object, SharedSessionContractImplementor session) throws HibernateException {
        LOG.trace("");
        super.delete(id, version, object, session);
    }

    @Override
    protected String generateUpdateString(boolean[] includeProperty, int j, Object[] oldFields, boolean useRowId) {
        LOG.trace("");
        if (isMySQLDialect) {
            return generateMySQLUpdateString(includeProperty, j, oldFields, useRowId);
        } else {
            return super.generateUpdateString(includeProperty, j, oldFields, useRowId);
        }
    }

    @Override
    public int dehydrateIdentityInsert(
            Object entity,
            PreparedStatement ps,
            SharedSessionContractImplementor session
    ) throws SQLException, HibernateException {
        LOG.trace("");
        return dehydrate(
                null,
                getPropertyValues(entity),
                getPropertyInsertability(),
                getPropertyColumnInsertable(),
                0,
                ps,
                session,
                false
        );
    }

    private void preLoad(SharedSessionContractImplementor session) {
        LOG.trace("");
        PhysicalConnectionHandlingMode mode1 = PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT;
        PhysicalConnectionHandlingMode mode2 = session.getJdbcCoordinator().getLogicalConnection().getConnectionHandlingMode();
        if (mode1.equals(mode2)) {
            MultiTenantConnectionProvider service = session.getFactory().getServiceRegistry().getService(MultiTenantConnectionProvider.class);
            MultiTenantConnectionProviderImpl.class.cast(service).setDataSourceKey(getEntityMetamodel().getName());
            session.getJdbcCoordinator().getLogicalConnection().manualDisconnect();
        }
    }

    private String generateMySQLUpdateString(boolean[] includeProperty, int j, Object[] oldFields, boolean useRowId) {
        LOG.trace("");
        List<String> columnNames = new LinkedList<>();
        List<String> idColumnNames = new LinkedList<>();
        boolean[][] propertyColumnUpdateable = getPropertyColumnUpdateable();
        int tableSpan = getTableSpan();
        int propertySpan = getEntityMetamodel().getPropertySpan();
        for (int tableIndex = 0; tableIndex < tableSpan; tableIndex++) {
            for (int propertyIndex = 0; propertyIndex < propertySpan; propertyIndex++) {
                if (includeProperty[propertyIndex] && isPropertyOfTable(propertyIndex, tableIndex)) {
                    String[] propertyColumnNames = getPropertyColumnNames(propertyIndex);
                    for (int columnIndex = 0; columnIndex < propertyColumnNames.length; columnIndex++) {
                        if (propertyColumnUpdateable[propertyIndex][columnIndex]) {
                            String columnName = propertyColumnNames[columnIndex];
                            columnNames.add(columnName);
                        }
                    }
                }
            }
            idColumnNames.addAll(Arrays.asList(getKeyColumns(tableIndex)));
        }
        String newUpdateString = Arrays.asList(
                "insert into ",
                getTableName(),
                Stream.concat(columnNames.stream(), idColumnNames.stream()).collect(Collectors.joining(", ", " (", ")")),
                IntStream.range(0, columnNames.size() + idColumnNames.size()).mapToObj(i -> "?").collect(Collectors.joining(", ", " values (", ")")),
                " on duplicate key update ",
                columnNames.stream().map(c -> c + " = values(" + c + ")").collect(Collectors.joining(", "))
        ).stream().collect(Collectors.joining());
        return newUpdateString;
    }

}
