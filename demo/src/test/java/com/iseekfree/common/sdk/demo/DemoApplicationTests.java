package com.iseekfree.common.sdk.demo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class DemoApplicationTests {

    private static final String JWT_SECRET = "demo-sdk-dev-only-jwt-secret-change-me-0001";
    private static final String TOKEN = demoToken();

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    void webContextAndGrpcAuthAreIntegrated() throws Exception {
        mockMvc.perform(get("/demo/public").header("x-demo-trace-id", "trace-demo-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.traceId").value("trace-demo-1"));

        mockMvc.perform(get("/demo/me").header("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.uid").value("u1"))
                .andExpect(jsonPath("$.data.domain").value("app.demo"));

        mockMvc.perform(get("/demo/grpc").header("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("echo:u1:ping"));

        mockMvc.perform(get("/demo/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-94));
    }

    @Test
    void uncaughtExceptionsUseTheUnifiedEnvelope() throws Exception {
        mockMvc.perform(get("/demo/error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-90))
                .andExpect(jsonPath("$.msg").value("boom"));

        mockMvc.perform(get("/demo/error/custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-91))
                .andExpect(jsonPath("$.msg").value("invalid argument: bad argument"));
    }

    @Test
    void atlasExceptionUsesTheUnifiedEnvelopeWithOptionalCode() throws Exception {
        mockMvc.perform(get("/demo/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-90))
                .andExpect(jsonPath("$.msg").value("business failure"));

        mockMvc.perform(get("/demo/business/custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-1001))
                .andExpect(jsonPath("$.msg").value("custom business failure"));
    }

    private static String demoToken() {
        return Jwts.builder()
                .claim("uid", "u1")
                .claim("domain", "app.demo")
                .claim("session", "session1")
                .claim("perms", "demo:read")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
