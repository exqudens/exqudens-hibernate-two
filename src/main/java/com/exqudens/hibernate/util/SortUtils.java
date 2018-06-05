package com.exqudens.hibernate.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.OneToOneType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SortUtils {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(SortUtils.class);
        LOG.trace("");
    }

    private SortUtils() {
        super();
        LOG.trace("");
    }

    /**
     * @author exqudens
     * @see org.hibernate.engine.spi.ActionQueue.InsertActionSorter.sort
     * @param entities
     * @param session
     * @return
     */
    public static List<List<Object>> sort(List<Object> insertions, SharedSessionContractImplementor session) {
        LOG.trace("");
        // optimize the hash size to eliminate a rehash.
        List<BatchIdentifier> latestBatches = new ArrayList<>();
        Map<Object, BatchIdentifier> entityBatchIdentifier = new HashMap<>(insertions.size() + 1, 1.0f);
        Map<BatchIdentifier, List<Object>> actionBatches = new HashMap<>();

        for (Object action : insertions) {
            EntityPersister entityPersister = session.getEntityPersister(null, action);
            BatchIdentifier batchIdentifier = new BatchIdentifier(
                entityPersister.getEntityName(), //action.getEntityName(),
                session.getFactory().getMetamodel().entityPersister(entityPersister.getEntityName()).getRootEntityName()
            );

            // the entity associated with the current action.
            Object currentEntity = action;
            int index = latestBatches.indexOf(batchIdentifier);

            if (index != -1) {
                batchIdentifier = latestBatches.get(index);
            } else {
                latestBatches.add(batchIdentifier);
            }
            addParentChildEntityNames(action, batchIdentifier, session);
            entityBatchIdentifier.put(currentEntity, batchIdentifier);
            addToBatch(batchIdentifier, action, actionBatches);
        }
        insertions.clear();

        // Examine each entry in the batch list, and build the dependency graph.
        for (int i = 0; i < latestBatches.size(); i++) {
            BatchIdentifier batchIdentifier = latestBatches.get(i);

            for (int j = i - 1; j >= 0; j--) {
                BatchIdentifier prevBatchIdentifier = latestBatches.get(j);
                if (prevBatchIdentifier.hasAnyParentEntityNames(batchIdentifier)) {
                    prevBatchIdentifier.parent = batchIdentifier;
                }
                if (batchIdentifier.hasAnyChildEntityNames(prevBatchIdentifier)) {
                    prevBatchIdentifier.parent = batchIdentifier;
                }
            }

            for (int j = i + 1; j < latestBatches.size(); j++) {
                BatchIdentifier nextBatchIdentifier = latestBatches.get(j);

                if (nextBatchIdentifier.hasAnyParentEntityNames(batchIdentifier)) {
                    nextBatchIdentifier.parent = batchIdentifier;
                }
                if (batchIdentifier.hasAnyChildEntityNames(nextBatchIdentifier)) {
                    nextBatchIdentifier.parent = batchIdentifier;
                }
            }
        }

        boolean sorted = false;

        long maxIterations = latestBatches.size() * latestBatches.size();
        long iterations = 0;

        sort: do {
            // Examine each entry in the batch list, sorting them based on parent/child association
            // as depicted by the dependency graph.
            iterations++;

            for (int i = 0; i < latestBatches.size(); i++) {
                BatchIdentifier batchIdentifier = latestBatches.get(i);

                // Iterate next batches and make sure that children types are after parents.
                // Since the outer loop looks at each batch entry individually and the prior loop will reorder
                // entries as well, we need to look and verify if the current batch is a child of the next
                // batch or if the current batch is seen as a parent or child of the next batch.
                for (int j = i + 1; j < latestBatches.size(); j++) {
                    BatchIdentifier nextBatchIdentifier = latestBatches.get(j);

                    if (
                        batchIdentifier.hasParent(nextBatchIdentifier) && !nextBatchIdentifier.hasParent(
                            batchIdentifier
                        )
                    ) {
                        latestBatches.remove(batchIdentifier);
                        latestBatches.add(j, batchIdentifier);

                        continue sort;
                    }
                }
            }
            sorted = true;
        } while (!sorted && iterations <= maxIterations);

        if (iterations > maxIterations) {
            LOG.warn(
                "The batch containing " + latestBatches.size() + " statements could not be sorted after "
                + maxIterations + " iterations. " + "This might indicate a circular entity relationship."
            );
        }

        // Now, rebuild the insertions list. There is a batch for each entry in the name list.
        /*for (BatchIdentifier rootIdentifier : latestBatches) {
            List<Object> batch = actionBatches.get(rootIdentifier);
            insertions.addAll(batch);
        }*/

        List<List<Object>> result = new ArrayList<>();
        for (BatchIdentifier rootIdentifier : latestBatches) {
            List<Object> batch = actionBatches.get(rootIdentifier);
            List<List<Object>> sortedBatch = sortBatch(batch, session);
            for (List<Object> l : sortedBatch) {
                if (!l.isEmpty()) {
                    result.add(l);
                }
            }
        }
        return result;
    }

    private static void addParentChildEntityNames(
        Object action,
        BatchIdentifier batchIdentifier,
        SharedSessionContractImplementor session
    ) {
        LOG.trace("");
        EntityPersister entityPersister = session.getEntityPersister(null, action);
        Object[] propertyValues = entityPersister.getPropertyValuesToInsert(action, Collections.emptyMap(), session);
        ClassMetadata classMetadata = entityPersister.getClassMetadata();
        if (classMetadata != null) {
            Type[] propertyTypes = classMetadata.getPropertyTypes();

            for (int i = 0; i < propertyValues.length; i++) {
                Object value = propertyValues[i];
                Type type = propertyTypes[i];
                addParentChildEntityNameByPropertyAndValue(action, batchIdentifier, type, value, session);
            }
        }
    }

    private static void addParentChildEntityNameByPropertyAndValue(
        Object action,
        BatchIdentifier batchIdentifier,
        Type type,
        Object value,
        SharedSessionContractImplementor session
    ) {
        LOG.trace("");
        if (type.isEntityType() && value != null) {
            final EntityType entityType = (EntityType) type;
            final String entityName = entityType.getName();
            final String rootEntityName = session.getFactory().getMetamodel().entityPersister(entityName)
            .getRootEntityName();

            if (
                entityType.isOneToOne() && OneToOneType.class.cast(entityType)
                .getForeignKeyDirection() == ForeignKeyDirection.TO_PARENT
            ) {
                batchIdentifier.getChildEntityNames().add(entityName);
                if (!rootEntityName.equals(entityName)) {
                    batchIdentifier.getChildEntityNames().add(rootEntityName);
                }
            } else {
                batchIdentifier.getParentEntityNames().add(entityName);
                if (!rootEntityName.equals(entityName)) {
                    batchIdentifier.getParentEntityNames().add(rootEntityName);
                }
            }
        } else if (type.isCollectionType() && value != null) {
            CollectionType collectionType = (CollectionType) type;
            final SessionFactoryImplementor sessionFactory = ((SessionImplementor) session).getSessionFactory();
            if (collectionType.getElementType(sessionFactory).isEntityType()) {
                String entityName = collectionType.getAssociatedEntityName(sessionFactory);
                String rootEntityName = session.getFactory().getMetamodel().entityPersister(entityName)
                .getRootEntityName();
                batchIdentifier.getChildEntityNames().add(entityName);
                if (!rootEntityName.equals(entityName)) {
                    batchIdentifier.getChildEntityNames().add(rootEntityName);
                }
            }
        } else if (type.isComponentType() && value != null) {
            // Support recursive checks of composite type properties for associations and collections.
            CompositeType compositeType = (CompositeType) type;
            //final SharedSessionContractImplementor session = action.getSession();
            Object[] componentValues = compositeType.getPropertyValues(value, session);
            for (int j = 0; j < componentValues.length; ++j) {
                Type componentValueType = compositeType.getSubtypes()[j];
                Object componentValue = componentValues[j];
                addParentChildEntityNameByPropertyAndValue(
                    action,
                    batchIdentifier,
                    componentValueType,
                    componentValue,
                    session
                );
            }
        }
    }

    private static void addToBatch(
        BatchIdentifier batchIdentifier,
        Object action,
        Map<BatchIdentifier, List<Object>> actionBatches
    ) {
        LOG.trace("");
        List<Object> actions = actionBatches.get(batchIdentifier);

        if (actions == null) {
            actions = new LinkedList<>();
            actionBatches.put(batchIdentifier, actions);
        }
        actions.add(action);
    }

    private static List<List<Object>> sortBatch(List<Object> entities, SharedSessionContractImplementor session) {
        LOG.trace("");
        List<Object> first = new ArrayList<>();
        List<Object> second = new ArrayList<>();
        for (Object entity : entities) {
            EntityPersister entityPersister = session.getEntityPersister(null, entity);
            Object[] propertyValues = entityPersister.getPropertyValuesToInsert(
                entity,
                Collections.emptyMap(),
                session
            );
            ClassMetadata classMetadata = entityPersister.getClassMetadata();
            boolean added = false;

            if (classMetadata != null) {
                Type[] propertyTypes = classMetadata.getPropertyTypes();

                for (int i = 0; i < propertyValues.length; i++) {
                    Object value = propertyValues[i];
                    Type type = propertyTypes[i];

                    if (entity.getClass().getName().equals(type.getReturnedClass().getName())) {
                        if (type.isEntityType() && value != null) {
                            second.add(entity);
                            added = true;
                            break;
                        } else if (
                            type.isCollectionType() && value != null && !Collection.class.cast(value).isEmpty()
                        ) {
                            first.add(entity);
                            added = true;
                            break;
                        }
                    }
                }
            }
            if (!added) {
                first.add(entity);
            }
        }
        List<List<Object>> sorted = new ArrayList<>();
        sorted.add(first);
        sorted.add(second);
        return sorted;
    }

    private static class BatchIdentifier {

        private final String entityName;
        private final String rootEntityName;

        private Set<String> parentEntityNames = new HashSet<>();

        private Set<String> childEntityNames = new HashSet<>();

        private BatchIdentifier parent;

        BatchIdentifier(String entityName, String rootEntityName) {
            this.entityName = entityName;
            this.rootEntityName = rootEntityName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BatchIdentifier)) {
                return false;
            }
            BatchIdentifier that = (BatchIdentifier) o;
            return Objects.equals(entityName, that.entityName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityName);
        }

        String getEntityName() {
            return entityName;
        }

        String getRootEntityName() {
            return rootEntityName;
        }

        Set<String> getParentEntityNames() {
            return parentEntityNames;
        }

        Set<String> getChildEntityNames() {
            return childEntityNames;
        }

        boolean hasAnyParentEntityNames(BatchIdentifier batchIdentifier) {
            return parentEntityNames.contains(batchIdentifier.getEntityName()) || parentEntityNames.contains(
                batchIdentifier.getRootEntityName()
            );
        }

        boolean hasAnyChildEntityNames(BatchIdentifier batchIdentifier) {
            return childEntityNames.contains(batchIdentifier.getEntityName());
        }

        boolean hasParent(BatchIdentifier batchIdentifier) {
            return (parent == batchIdentifier || (parentEntityNames.contains(batchIdentifier.getEntityName()))
            || parent != null && parent.hasParent(batchIdentifier, new ArrayList<>()));
        }

        private boolean hasParent(BatchIdentifier batchIdentifier, List<BatchIdentifier> stack) {
            if (!stack.contains(this) && parent != null) {
                stack.add(this);
                return parent.hasParent(batchIdentifier, stack);
            }
            return (parent == batchIdentifier || parentEntityNames.contains(batchIdentifier.getEntityName()));
        }
    }

}
