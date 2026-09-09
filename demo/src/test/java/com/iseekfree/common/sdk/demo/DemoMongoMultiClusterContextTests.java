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
        "claw.mongo.enabled=true",
        "claw.mongo.auto-index=false",
        "claw.redis.enabled=false",
        "claw.grpc.server.enabled=false",
        "claw.grpc.client.enabled=false"
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
        assertThat(catalog.getDatabase().getName()).isEqualTo("claw_demo_catalog");
        assertThat(audit.getDatabase().getName()).isEqualTo("claw_demo_audit");
        assertThat(sales.getDatabase().getName()).isEqualTo("claw_demo_sales");
        assertThat(archive.getDatabase().getName()).isEqualTo("claw_demo_archive");
        assertThat(catalogProductService).isNotNull();
        assertThat(auditEventService).isNotNull();
        assertThat(salesOrderService).isNotNull();
        assertThat(archivedOrderService).isNotNull();
    }
}
