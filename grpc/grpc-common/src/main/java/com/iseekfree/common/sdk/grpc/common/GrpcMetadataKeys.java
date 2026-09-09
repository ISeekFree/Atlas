package com.iseekfree.common.sdk.grpc.common;

import io.grpc.Metadata;

public final class GrpcMetadataKeys {

    public static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> ACCESS_TOKEN =
            Metadata.Key.of("accessToken", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> GRPC_TOKEN =
            Metadata.Key.of("grpcToken", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> X_FORWARDED_FOR =
            Metadata.Key.of("x-forwarded-for", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> X_REAL_IP =
            Metadata.Key.of("x-real-ip", Metadata.ASCII_STRING_MARSHALLER);

    private GrpcMetadataKeys() {
    }
}
