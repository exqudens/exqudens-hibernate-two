package com.exqudens.hibernate.persister;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

public interface Persister extends org.hibernate.id.PostInsertIdentityPersister {

    void insert(List<Object> entities, SharedSessionContractImplementor session);

    void update(List<Object> entities, SharedSessionContractImplementor session);

    void delete(List<Object> entities, SharedSessionContractImplementor session);

    List<Entry<Serializable, Object>> insertIdentity(List<Object> entities, SharedSessionContractImplementor session);

}
