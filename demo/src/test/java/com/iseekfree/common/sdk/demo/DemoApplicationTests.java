package com.iseekfree.common.sdk.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class DemoApplicationTests {

    private static final String TOKEN = "_u_=u1;_d_=app.demo;_s_=session1;_exp_=4102444800000;_perms_=demo:read";

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
}
