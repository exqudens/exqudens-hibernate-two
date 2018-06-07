package com.exqudens.hibernate.persister;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

public interface PostInsertIdentityPersister extends org.hibernate.id.PostInsertIdentityPersister {

    List<Entry<Serializable, Object>> insert(List<Object> entities, SharedSessionContractImplementor session);

    void update(List<Object> entities, SharedSessionContractImplementor session);

    void delete(List<Object> entities, SharedSessionContractImplementor session);

}
