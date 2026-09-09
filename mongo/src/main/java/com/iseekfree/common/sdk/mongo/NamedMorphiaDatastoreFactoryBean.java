package com.iseekfree.common.sdk.mongo;

import dev.morphia.Datastore;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * Resolves one named datastore from the registry while still allowing normal
 * constructor injection during Spring bean creation.
 */
public class NamedMorphiaDatastoreFactoryBean implements FactoryBean<Datastore>, BeanFactoryAware {

    private final String datastoreName;
    private BeanFactory beanFactory;

    public NamedMorphiaDatastoreFactoryBean(String datastoreName) {
        this.datastoreName = datastoreName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Datastore getObject() {
        return beanFactory.getBean(MorphiaDatastoreRegistry.class)
                .getDatastore(datastoreName)
                .orElseThrow(() -> new IllegalStateException("Unknown Mongo datastore: " + datastoreName));
    }

    @Override
    public Class<?> getObjectType() {
        return Datastore.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
