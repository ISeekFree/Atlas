package com.iseekfree.common.sdk.demo.mongo.primary.catalog;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import org.bson.types.ObjectId;

@Entity("catalog_categories")
@Indexes(@Index(
        fields = {@Field("parentCode"), @Field("code")},
        options = @IndexOptions(name = "uk_catalog_category_path", unique = true)
))
public class CatalogCategory {

    @Id
    private ObjectId id;
    private String parentCode;
    private String code;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
