package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.demo.mongo.primary.audit.AuditEventService;
import com.iseekfree.common.sdk.demo.mongo.primary.catalog.CatalogProductService;
import com.iseekfree.common.sdk.demo.mongo.secondary.archive.ArchivedOrderService;
import com.iseekfree.common.sdk.demo.mongo.secondary.sales.SalesOrderService;
import dev.morphia.Datastore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "framework.mongo.enabled=true",
        "framework.mongo.auto-index=false",
        "framework.redis.enabled=false",
        "framework.grpc.server.enabled=false",
        "framework.grpc.client.enabled=false"
})
class DemoMongoMultiClusterContextTests {

    @Autowired
    @Qualifier("catalog")
    private Datastore catalog;

    @Autowired
    @Qualifier("audit")
    private Datastore audit;

    @Autowired
    @Qualifier("sales")
    private Datastore sales;

    @Autowired
    @Qualifier("archive")
    private Datastore archive;

    @Autowired
    private CatalogProductService catalogProductService;

    @Autowired
    private AuditEventService auditEventService;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private ArchivedOrderService archivedOrderService;

    @Test
    void injectsEachNamedDatastoreIntoItsService() {
        assertThat(catalog.getDatabase().getName()).isEqualTo("framework_demo_catalog");
        assertThat(audit.getDatabase().getName()).isEqualTo("framework_demo_audit");
        assertThat(sales.getDatabase().getName()).isEqualTo("framework_demo_sales");
        assertThat(archive.getDatabase().getName()).isEqualTo("framework_demo_archive");
        assertThat(catalogProductService).isNotNull();
        assertThat(auditEventService).isNotNull();
        assertThat(salesOrderService).isNotNull();
        assertThat(archivedOrderService).isNotNull();
    }
}
