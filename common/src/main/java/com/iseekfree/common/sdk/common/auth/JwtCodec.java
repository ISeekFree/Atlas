package com.iseekfree.common.sdk.common.auth;

import com.iseekfree.common.sdk.common.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Framework-level JWT crypto: it signs and verifies tokens and hands back the
 * raw claim map.
 *
 * <p>The SDK deliberately stops there. It never understands business claim
 * names such as a user id, session, tenant or permission list; the consuming
 * application defines its own claim structure and maps it onto its
 * {@link com.iseekfree.common.sdk.common.ctx.WebContext} subclass inside a
 * {@link com.iseekfree.common.sdk.common.ctx.WebContextLoader}. The signing
 * algorithm is inferred from the supplied key, so supporting HMAC, RSA or EC
 * only means handing in a different key.</p>
 */
public final class JwtCodec {

    private final Key signingKey;
    private final Key verificationKey;

    private JwtCodec(Key signingKey, Key verificationKey) {
        this.signingKey = signingKey;
        this.verificationKey = verificationKey;
    }

    /** Symmetric helper for the common HS256/384/512 shared-secret setup. */
    public static JwtCodec hmac(SecretKey key) {
        return new JwtCodec(key, key);
    }

    /** Asymmetric helper for RSA/EC setups where only the public half verifies. */
    public static JwtCodec asymmetric(PrivateKey signingKey, PublicKey verificationKey) {
        return new JwtCodec(signingKey, verificationKey);
    }

    /** Signs the given business claims. The caller owns every claim name. */
    public String encode(Map<String, ?> claims) {
        if (signingKey == null) {
            throw new IllegalStateException("JWT signing key is not configured");
        }
        return Jwts.builder()
                .claims(claims == null ? Map.of() : claims)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Verifies the signature and the registered time claims, returning the raw
     * claim map. Throws {@link UnauthorizedException} for any invalid token so
     * the auth boundary stays fail-closed.
     */
    public Map<String, Object> verify(String token) {
        return tryVerify(token).orElseThrow(() -> new UnauthorizedException("Invalid or expired token"));
    }

    /** Lenient variant used by callers that prefer an empty result over a throw. */
    public Optional<Map<String, Object>> tryVerify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        if (verificationKey == null) {
            return Optional.empty();
        }
        try {
            JwtParserBuilder parser = Jwts.parser();
            if (verificationKey instanceof SecretKey secretKey) {
                parser.verifyWith(secretKey);
            } else if (verificationKey instanceof PublicKey publicKey) {
                parser.verifyWith(publicKey);
            } else {
                throw new IllegalStateException("Unsupported JWT verification key type");
            }
            Claims claims = parser.build().parseSignedClaims(token).getPayload();
            return Optional.of(new LinkedHashMap<>(claims));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
