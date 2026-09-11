package com.iseekfree.common.sdk.grpc.common;

import com.iseekfree.common.sdk.common.auth.AuthIdentity;
import io.grpc.Context;

import java.util.Optional;

public final class GrpcAuthContext {

    public static final Context.Key<AuthIdentity> IDENTITY = Context.key("atlas-auth-identity");
    public static final Context.Key<String> USER_ID = Context.key("uid");
    public static final Context.Key<String> DOMAIN = Context.key("domain");
    public static final Context.Key<String> IP = Context.key("ip");

    private GrpcAuthContext() {
    }

    public static Optional<AuthIdentity> identity() {
        return Optional.ofNullable(IDENTITY.get());
    }

    public static Optional<String> userId() {
        return Optional.ofNullable(USER_ID.get());
    }

    public static Context withIdentity(Context context, AuthIdentity identity, String ip) {
        Context next = context.withValue(IDENTITY, identity)
                .withValue(USER_ID, identity.getUserId())
                .withValue(IP, ip);
        if (identity.getDomain().isPresent()) {
            next = next.withValue(DOMAIN, identity.getDomain().get());
        }
        return next;
    }
}
