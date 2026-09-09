package com.iseekfree.common.sdk.demo.mongo.primary.audit;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.utils.IndexDirection;
import org.bson.types.ObjectId;

import java.time.Instant;

@Entity("audit_events")
public class AuditEvent {

    @Id
    private ObjectId id;
    private String operatorId;

    @Indexed(value = IndexDirection.ASC, options = @IndexOptions(
            name = "ttl_audit_event_created_at", expireAfterSeconds = 2_592_000
    ))
    private Instant createdAt;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
