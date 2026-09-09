package com.iseekfree.common.sdk.demo.mongo.secondary.sales;

import dev.morphia.Datastore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "claw.mongo", name = "enabled", havingValue = "true")
public class SalesOrderService {

    private final Datastore datastore;

    public SalesOrderService(@Qualifier("sales") Datastore datastore) {
        this.datastore = datastore;
    }

    public SalesOrder save(SalesOrder order) {
        return datastore.save(order);
    }
}
