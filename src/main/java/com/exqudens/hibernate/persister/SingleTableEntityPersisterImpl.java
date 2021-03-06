package com.exqudens.hibernate.persister;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.action.internal.DelayedPostInsertIdentifier;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.jdbc.Expectation;
import org.hibernate.jdbc.Expectations;
import org.hibernate.loader.entity.CascadeEntityLoader;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.MultiLoadOptions;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.resource.jdbc.spi.PhysicalConnectionHandlingMode;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exqudens.hibernate.multitenancy.MultiTenantConnectionProviderImpl;

public class SingleTableEntityPersisterImpl extends SingleTableEntityPersister implements Persister {

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
            IntStream.range(0, updateResultCheckStyles.length).forEach(
                i -> updateResultCheckStyles[i] = ExecuteUpdateResultCheckStyle.NONE
            );
        }
    }

    @Override
    public void insert(Serializable id, Object[] fields, Object object, SharedSessionContractImplementor session) {
        LOG.trace("");
        super.insert(id, fields, object, session);
    }

    @Override
    protected Serializable insert(
        Object[] fields,
        boolean[] notNull,
        String sql,
        Object object,
        SharedSessionContractImplementor session
    ) throws HibernateException {
        LOG.trace("");
        return super.insert(fields, notNull, sql, object, session);
    }

    @Override
    protected UniqueEntityLoader getAppropriateLoader(
        LockOptions lockOptions,
        SharedSessionContractImplementor session
    ) {
        UniqueEntityLoader loader = super.getAppropriateLoader(lockOptions, session);
        if (loader instanceof CascadeEntityLoader) {
            return (UniqueEntityLoader) getLoaders().get(LockMode.READ);
        }
        return loader;
    }

    @Override
    public Object load(
        Serializable id,
        Object optionalObject,
        LockMode lockMode,
        SharedSessionContractImplementor session
    ) {
        LOG.trace("");
        preLoad(session);
        return super.load(id, optionalObject, lockMode, session);
    }

    @Override
    public Object load(
        Serializable id,
        Object optionalObject,
        LockOptions lockOptions,
        SharedSessionContractImplementor session
    ) throws HibernateException {
        LOG.trace("");
        preLoad(session);
        return super.load(id, optionalObject, lockOptions, session);
    }

    @Override
    public Object loadByUniqueKey(String propertyName, Object uniqueKey, SharedSessionContractImplementor session)
    throws HibernateException {
        LOG.trace("");
        preLoad(session);
        return super.loadByUniqueKey(propertyName, uniqueKey, session);
    }

    @Override
    public Serializable loadEntityIdByNaturalId(
        Object[] naturalIdValues,
        LockOptions lockOptions,
        SharedSessionContractImplementor session
    ) {
        LOG.trace("");
        preLoad(session);
        return super.loadEntityIdByNaturalId(naturalIdValues, lockOptions, session);
    }

    @Override
    public List<?> multiLoad(
        Serializable[] ids,
        SharedSessionContractImplementor session,
        MultiLoadOptions loadOptions
    ) {
        LOG.trace("");
        preLoad(session);
        return super.multiLoad(ids, session, loadOptions);
    }

    @Override
    public void update(
        Serializable id,
        Object[] fields,
        int[] dirtyFields,
        boolean hasDirtyCollection,
        Object[] oldFields,
        Object oldVersion,
        Object object,
        Object rowId,
        SharedSessionContractImplementor session
    ) throws HibernateException {
        LOG.trace("");
        super.update(id, fields, dirtyFields, hasDirtyCollection, oldFields, oldVersion, object, rowId, session);
    }

    @Override
    public void delete(Serializable id, Object version, Object object, SharedSessionContractImplementor session)
    throws HibernateException {
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
    public void insert(List<Object> entities, SharedSessionContractImplementor session) {
        LOG.trace("");
        boolean[] propertyInsertability = getPropertyInsertability();
        String insertSQL = generateInsertString(false, propertyInsertability);
        PreparedStatement ps = null;

        try {

            for (List<Object> batch : toBatches(entities, getJdbcBatchSize(session))) {

                ps = session.getJdbcCoordinator().getLogicalConnection().getPhysicalConnection().prepareStatement(insertSQL);

                for (Object entity : batch) {

                    Serializable id = getIdentifier(entity, session);
                    Object[] fields = getPropertyValues(entity);
                    boolean[][] propertyColumnInsertable = getPropertyColumnInsertable();
                    int j = 0;
                    int index = 1;
                    Expectation expectation = Expectations.appropriateExpectation(insertResultCheckStyles[j]);
                    index += expectation.prepare( ps );

                    dehydrate( id, fields, null, propertyInsertability, propertyColumnInsertable, j, ps, session, index, false );

                    session.getJdbcServices().getSqlStatementLogger().logStatement(insertSQL);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (RuntimeException e) {
            LOG.error(insertSQL, e);
            throw e;
        } catch (Exception e) {
            LOG.error(insertSQL, e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            LOG.error(insertSQL, e);
            throw new RuntimeException(e);
        } finally {
            session.getJdbcCoordinator().getLogicalConnection().getResourceRegistry().release(ps);
            session.getJdbcCoordinator().afterStatementExecution();
        }
    }

    @Override
    public List<Entry<Serializable, Object>> insertIdentity(List<Object> entities, SharedSessionContractImplementor session) {
        LOG.trace("");
        String insertSQL = generateIdentityInsertString(getPropertyInsertability());
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            List<Entry<Serializable, Object>> idDelayedUpdates = new LinkedList<>();
            for (List<Object> batch : toBatches(entities, getJdbcBatchSize(session))) {
                ps = session.getJdbcCoordinator().getLogicalConnection().getPhysicalConnection().prepareStatement(
                    insertSQL,
                    PreparedStatement.RETURN_GENERATED_KEYS
                );
                List<Object> delayedUpdateEntities = new ArrayList<>();
                for (Object entity : batch) {

                    Object delayedUpdateEntity = null;
                    Type[] propertyTypes = getPropertyTypes();
                    Object[] propertyValues = getPropertyValues(entity);
                    for (int i = 0; i < propertyTypes.length; i++) {
                        if (
                            propertyTypes[i].isAssociationType() && !propertyTypes[i].isCollectionType() && session
                            .getPersistenceContext().getEntry(propertyValues[i]) != null
                        ) {
                            Serializable identifier = session.getPersistenceContext().getEntry(propertyValues[i])
                            .getEntityKey().getIdentifier();
                            if (identifier != null && identifier instanceof DelayedPostInsertIdentifier) {
                                propertyValues[i] = null;
                                delayedUpdateEntity = entity;
                            }
                        }
                    }
                    delayedUpdateEntities.add(delayedUpdateEntity);

                    dehydrate(
                        null,
                        propertyValues,
                        getPropertyInsertability(),
                        getPropertyColumnInsertable(),
                        0,
                        ps,
                        session,
                        false
                    );
                    session.getJdbcServices().getSqlStatementLogger().logStatement(insertSQL);

                    ps.addBatch();
                }
                ps.executeBatch();
                rs = ps.getGeneratedKeys();
                for (Object delayedUpdateEntity : delayedUpdateEntities) {
                    rs.next();
                    Serializable id = IdentifierGeneratorHelper.get(
                        rs,
                        getRootTableKeyColumnNames()[0],
                        getIdentifierType(),
                        session.getJdbcServices().getJdbcEnvironment().getDialect()
                    );
                    idDelayedUpdates.add(new SimpleEntry<>(id, delayedUpdateEntity));
                }
            }
            return idDelayedUpdates;
        } catch (RuntimeException e) {
            LOG.error(insertSQL, e);
            throw e;
        } catch (Exception e) {
            LOG.error(insertSQL, e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            LOG.error(insertSQL, e);
            throw new RuntimeException(e);
        } finally {
            session.getJdbcCoordinator().getLogicalConnection().getResourceRegistry().release(rs, ps);
            session.getJdbcCoordinator().afterStatementExecution();
        }
    }

    @Override
    public void update(List<Object> entities, SharedSessionContractImplementor session) {
        LOG.trace("");
        // TODO
    }

    @Override
    public void delete(List<Object> entities, SharedSessionContractImplementor session) {
        LOG.trace("");
        SingleTableEntityPersisterImpl persister = null;
        List<Serializable> keys = null;
        PreparedStatement ps = null;
        try {

            for (List<Object> batch : toBatches(entities, getJdbcBatchSize(session))) {

                if (!batch.isEmpty()) {

                    if (persister == null) {
                        EntityPersister entityPersister = session.getEntityPersister(null, batch.get(0));
                        persister = SingleTableEntityPersisterImpl.class.cast(entityPersister);
                    }

                    String[] keyColumns = persister.getKeyColumns(0);

                    if (keyColumns.length == 1) {

                        keys = new LinkedList<>();

                        for (Object entity : batch) {
                            Serializable key = persister.getIdentifier(entity, session);
                            keys.add(key);
                        }

                        String sql = Arrays.asList(
                            "delete from ",
                            persister.getTableName(0),
                            " where ",
                            keyColumns[0],
                            " in (",
                            keys.stream().map(s -> "?").collect(Collectors.joining(", ")),
                            ")"
                        ).stream().collect(Collectors.joining());
                        ps = session.getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, false);

                        for (int j = 0; j < keys.size(); j++) {
                            ps.setObject(j + 1, keys.get(j));
                        }
                        ps.executeUpdate();
                    } else {

                        for (Object entity : batch) {
                            Serializable identifier = persister.getIdentifier(entity, session);
                            Object vers = persister.getVersion(entity);
                            persister.delete(identifier, vers, entity, session);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ps != null) {
                session.getJdbcCoordinator().getLogicalConnection().getResourceRegistry().release(ps);
                session.getJdbcCoordinator().afterStatementExecution();
            }
        }
    }

    private <E> List<List<E>> toBatches(List<E> list, int size) {
        Function<Entry<Integer, E>, Integer> classifier;
        classifier = (Entry<Integer, E> e) -> {
            int number = e.getKey().intValue();
            if (number % size != 0) {
                return number / size * size + size;
            } else {
                return number;
            }
        };
        Function<Entry<Integer, E>, E> mapper = (Entry<Integer, E> e) -> e.getValue();
        return IntStream.range(0, list.size()).mapToObj(i -> new SimpleEntry<>(i + 1, list.get(i))).collect(
            Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.mapping(mapper, Collectors.toList()))
        ).values().stream().collect(Collectors.toList());
    }

    private int getJdbcBatchSize(SharedSessionContractImplementor session) {
        return session.getJdbcBatchSize() != null ? session.getJdbcBatchSize()
        : session.getFactory().getSessionFactoryOptions().getJdbcBatchSize();
    }

    private void preLoad(SharedSessionContractImplementor session) {
        LOG.trace("");
        PhysicalConnectionHandlingMode mode1 = PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT;
        PhysicalConnectionHandlingMode mode2 = session.getJdbcCoordinator().getLogicalConnection()
        .getConnectionHandlingMode();
        if (mode1.equals(mode2)) {
            MultiTenantConnectionProvider service = session.getFactory().getServiceRegistry().getService(
                MultiTenantConnectionProvider.class
            );
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
            IntStream.range(0, columnNames.size() + idColumnNames.size()).mapToObj(i -> "?").collect(
                Collectors.joining(", ", " values (", ")")
            ),
            " on duplicate key update ",
            columnNames.stream().map(c -> c + " = values(" + c + ")").collect(Collectors.joining(", "))
        ).stream().collect(Collectors.joining());
        return newUpdateString;
    }

}
