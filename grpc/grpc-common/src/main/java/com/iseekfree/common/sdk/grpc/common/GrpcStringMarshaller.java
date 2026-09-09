package com.iseekfree.common.sdk.grpc.common;

import io.grpc.MethodDescriptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class GrpcStringMarshaller implements MethodDescriptor.Marshaller<String> {

    @Override
    public InputStream stream(String value) {
        return new ByteArrayInputStream((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String parse(InputStream stream) {
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Unable to parse gRPC string payload", ex);
        }
    }
}
