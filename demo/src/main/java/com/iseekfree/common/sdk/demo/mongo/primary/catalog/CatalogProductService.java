package com.iseekfree.common.sdk.demo.mongo.primary.catalog;

import dev.morphia.Datastore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "claw.mongo", name = "enabled", havingValue = "true")
public class CatalogProductService {

    private final Datastore datastore;

    public CatalogProductService(@Qualifier("catalog") Datastore datastore) {
        this.datastore = datastore;
    }

    public CatalogProduct save(CatalogProduct product) {
        return datastore.save(product);
    }
}
