package com.iseekfree.common.sdk.demo.mongo.secondary.archive;

import dev.morphia.Datastore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "claw.mongo", name = "enabled", havingValue = "true")
public class ArchivedOrderService {

    private final Datastore datastore;

    public ArchivedOrderService(@Qualifier("archive") Datastore datastore) {
        this.datastore = datastore;
    }

    public ArchivedOrder save(ArchivedOrder order) {
        return datastore.save(order);
    }
}
