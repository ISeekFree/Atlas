package com.iseekfree.common.sdk.common.auth;

public interface AuthService {

    AuthIdentity authenticate(AuthRequest request);

    default boolean isPermitted(AuthIdentity identity, String[] permissions) {
        return identity != null && identity.hasAnyPermission(permissions);
    }
}
