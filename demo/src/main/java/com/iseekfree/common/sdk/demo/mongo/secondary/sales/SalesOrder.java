package com.iseekfree.common.sdk.demo.mongo.secondary.sales;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import org.bson.types.ObjectId;

@Entity("sales_orders")
@Indexes(@Index(
        fields = {@Field("tenantId"), @Field("orderNo")},
        options = @IndexOptions(name = "uk_sales_order_tenant_no", unique = true)
))
public class SalesOrder {

    @Id
    private ObjectId id;
    private String tenantId;
    private String orderNo;

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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
