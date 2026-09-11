package com.iseekfree.common.sdk.demo.mongo.primary.audit;

import dev.morphia.Datastore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "framework.mongo", name = "enabled", havingValue = "true")
public class AuditEventService {

    private final Datastore datastore;

    public AuditEventService(@Qualifier("audit") Datastore datastore) {
        this.datastore = datastore;
    }

    public AuditEvent save(AuditEvent event) {
        return datastore.save(event);
    }
}
