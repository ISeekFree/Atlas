package com.iseekfree.common.sdk.demo.mongo.secondary.sales;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

@Entity("customer_profiles")
public class CustomerProfile {

    @Id
    private ObjectId id;

    @Indexed(options = @IndexOptions(name = "uk_customer_external_id", unique = true))
    private String externalId;

    private String displayName;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
