package com.iseekfree.common.sdk.demo.mongo.secondary.archive;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

import java.time.Instant;

@Entity("archived_orders")
@Indexes(@Index(
        fields = {@Field("tenantId"), @Field(value = "archivedAt", type = IndexType.DESC)},
        options = @IndexOptions(name = "idx_archived_order_tenant_time")
))
public class ArchivedOrder {

    @Id
    private ObjectId id;
    private String tenantId;
    private Instant archivedAt;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }
}
