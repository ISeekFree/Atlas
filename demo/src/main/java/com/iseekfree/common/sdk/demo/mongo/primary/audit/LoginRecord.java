package com.iseekfree.common.sdk.demo.mongo.primary.audit;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

import java.time.Instant;

@Entity("login_records")
@Indexes(@Index(
        fields = {@Field("accountId"), @Field(value = "loginAt", type = IndexType.DESC)},
        options = @IndexOptions(name = "idx_login_account_time")
))
public class LoginRecord {

    @Id
    private ObjectId id;
    private String accountId;
    private Instant loginAt;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Instant getLoginAt() {
        return loginAt;
    }

    public void setLoginAt(Instant loginAt) {
        this.loginAt = loginAt;
    }
}
