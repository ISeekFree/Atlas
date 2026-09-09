package com.iseekfree.common.sdk.demo;

import com.iseekfree.common.sdk.grpc.client.inject.GrpcClient;
import com.iseekfree.common.sdk.web.context.AuthRequired;
import com.iseekfree.common.sdk.web.context.WebContext;
import com.iseekfree.common.sdk.web.flux.WebContextFlux;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.stub.ClientCalls;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @GrpcClient("local")
    private Channel localChannel;

    @GetMapping("/public")
    public Map<String, String> publicInfo(WebContext context) {
        return Map.of(
                "ip", context.getIp() == null ? "" : context.getIp(),
                "traceId", context.getAttribute("demoTraceId").map(Object::toString).orElse("")
        );
    }

    @AuthRequired(domains = "app.demo", perms = "demo:read")
    @GetMapping("/me")
    public Map<String, String> me(WebContext context) {
        return Map.of(
                "uid", context.getUid(),
                "domain", context.getDomain()
        );
    }

    @AuthRequired(domains = "app.demo")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(WebContext context) {
        return WebContextFlux.withContext(
                Flux.just("uid:" + context.getUid(), "domain:" + context.getDomain()),
                context
        );
    }

    @AuthRequired(domains = "app.demo")
    @GetMapping("/grpc")
    public String grpc(WebContext context) {
        return ClientCalls.blockingUnaryCall(localChannel, DemoGrpcService.METHOD, CallOptions.DEFAULT, "ping");
    }
}
