package com.iseekfree.common.sdk.grpc.server;

import io.grpc.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GrpcStatusExceptionResponseResolverTest {

    private final GrpcStatusExceptionResponseResolver resolver = new GrpcStatusExceptionResponseResolver();

    @Test
    void mapsDownstreamGrpcStatusesToHttpStatuses() {
        assertEquals(404, body(Status.NOT_FOUND).httpStatus());
        assertEquals(503, body(Status.UNAVAILABLE).httpStatus());
        assertEquals(503, body(Status.DEADLINE_EXCEEDED).httpStatus());
        assertEquals(502, body(Status.PERMISSION_DENIED).httpStatus());
        assertEquals(502, body(Status.UNKNOWN).httpStatus());
    }

    @Test
    void mapsUnauthenticatedToTheUnifiedLoginFailureCode() {
        assertEquals(401, body(Status.UNAUTHENTICATED).httpStatus());
        assertEquals(-94, body(Status.UNAUTHENTICATED).code());
    }

    @Test
    void keepsTheGrpcDescriptionWhenPresent() {
        assertEquals("missing user", body(Status.NOT_FOUND.withDescription("missing user")).msg());
    }

    @Test
    void ignoresFailuresThatAreNotGrpcStatuses() {
        assertNull(resolver.resolve(new IllegalStateException("boom"), null));
    }

    private com.iseekfree.common.sdk.web.mvc.ExceptionResponse body(Status status) {
        return resolver.resolve(status.asRuntimeException(), null);
    }
}
