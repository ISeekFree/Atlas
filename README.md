# sdk-webmvc

Spring Boot 4.x SDK for WebMVC services. It provides reusable auto-configuration for:

- WebMVC response wrapping, `WebContext` building/injection, `@AuthRequired`, and unified auth extension points. HTTP auth interception is business-owned.
- Morphia-style MongoDB datastores with package mapping and index creation.
- Redis `RedisTemplate` and `StringRedisTemplate`.
- gRPC server/client integration with shared auth contracts and metadata keys; inbound and outbound auth interceptors are declared by the consuming app.

AI model/provider integration is intentionally owned by consuming applications and is not included in this SDK.

## Build

Use Java 17:

```bash
./build.sh
```

## Deploy

```bash
./deploy.sh
```

`deploy.sh` increments the root `<revision>` before publishing, then excludes `demo`; that module is intentionally local validation only.

## Minimal Consumer Dependency

`sdk-bom` only manages compatible module versions; it adds no runtime dependencies. `sdk-starter` is the convenience dependency that actually brings in all SDK feature modules. Consumers that need only selected features can import the BOM and depend on individual modules instead of the starter.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.iseekfree.common</groupId>
      <artifactId>sdk-bom</artifactId>
      <version>4.1.0_v010</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.iseekfree.common</groupId>
    <artifactId>sdk-starter</artifactId>
  </dependency>
</dependencies>
```

Tokens are JWT. Atlas ships `JwtCodec` (crypto only) and no claim schema: set `framework.web.auth.jwt.secret`, then register a `WebContextProvider` for your context subtype and a `WebContextLoader` that verifies the token and fills it. The same context/loader pair is shared by `@AuthRequired`, HTTP controllers, gRPC services and WebSocket sessions; HTTP enforcement stays in the application's own `HandlerInterceptor` (the demo's `DemoWebAuthInterceptor`), and permission checks for `@AuthRequired(perms = ...)` go to an optional `WebContextAuthorizer`.

Each transport exposes a base context you can use directly or extend: `WebContext` (HTTP, injected into a controller parameter), `GrpcContext` (gRPC, read with `GrpcContext.current()`), and `WebsocketContext` (WebSocket, stored on the session attributes). All three extend the transport-neutral `AtlasContext`, so shared loaders and business fields live on one parent type.

## gRPC Client Targets

Atlas registers `static://host:port` for a fixed address and makes `static` the default resolver. A target without a scheme, such as `127.0.0.1:19090` or `account.internal:19090`, is therefore treated as static; IPv4, domain names, and bracketed IPv6 are supported. The SDK includes the official `dns`, `xds`, and Netty `unix` providers. Service discovery uses an explicit scheme: `dns:///service:port` for DNS or Kubernetes Services, `xds:///service-name` with the xDS runtime/bootstrap, or `unix:///path/to.sock` for Unix-domain sockets when native transport support is available.

## Extension Points

- Register a `WebContextCustomizer` bean to add business attributes to `WebContext`.
- CORS is SDK-owned: configure `framework.web.cors.*` and Atlas registers a `HIGHEST_PRECEDENCE` `CorsFilter` (path `/**` by default); applications no longer declare their own `CorsFilter` or `addCorsMappings`.
- Throw `AtlasException` from any handler and it is converted to the `{code,msg,data}` envelope. The code is optional: `new AtlasException("msg")` returns `code = -90` (`AtlasException.DEFAULT_CODE`), while `new AtlasException(-1001, "msg")` keeps the explicit business code; `IllegalArgumentException` is answered with `404`, and anything else uncaught returns `code = -90`. Register an `ExceptionResponseResolver` bean (returning an `ExceptionResponse`) to map specific exceptions onto your own code, message and HTTP status. A service using `sdk-grpc-server` also gets the downstream `StatusRuntimeException` mapping for free (`NOT_FOUND → 404`, `UNAVAILABLE`/`DEADLINE_EXCEEDED → 503`, other → `502`, and `UNAUTHENTICATED → 401` with the unified `code = -94`).
- Configure MongoDB with `framework.mongo.clusters.*` for multiple independent Mongo clusters. Define all databases and mapped packages under each cluster's explicit `datastores` map; datastore names must be globally unique.

The runnable multi-cluster Mongo example is in `demo/src/main/resources/application.yaml`. It contains two clusters, two databases per cluster, two collections per database, automatic unique/compound/TTL index examples, and qualified `Datastore` injection into separate services.
