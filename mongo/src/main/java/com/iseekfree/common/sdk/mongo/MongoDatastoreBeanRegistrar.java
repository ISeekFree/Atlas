package com.iseekfree.common.sdk.mongo;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Registers every configured Morphia datastore as a named Spring bean.
 */
public class MongoDatastoreBeanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AtlasMongoProperties properties = Binder.get(environment)
                .bind("framework.mongo", Bindable.of(AtlasMongoProperties.class))
                .orElseGet(AtlasMongoProperties::new);
        if (!properties.isEnabled()) {
            return;
        }

        for (String datastoreName : AtlasMongoAutoConfiguration.datastoreNames(properties)) {
            if (registry.containsBeanDefinition(datastoreName) || registry.isAlias(datastoreName)) {
                throw new BeanDefinitionStoreException("Cannot register Mongo datastore bean '"
                        + datastoreName + "' because that bean name is already in use");
            }
            RootBeanDefinition definition = new RootBeanDefinition(NamedMorphiaDatastoreFactoryBean.class);
            definition.getConstructorArgumentValues().addIndexedArgumentValue(0, datastoreName);
            registry.registerBeanDefinition(datastoreName, definition);
        }
    }
}
