# Atlas SDK WebMVC Architecture

## Baseline

- Runtime target: JDK 17.
- Framework baseline: Spring Boot 4.1.0 and Spring Framework 7.x.
- Integration versions: gRPC 1.82.1, protobuf 3.25.8, Morphia 2.5.3, MongoDB Java driver 5.8.0.
- Maven coordinates use groupId `com.iseekfree.common`.
- Java packages must start with `com.iseekfree.common.sdk`.

## Modules

| Module | Role |
| --- | --- |
| `common` | Shared auth contracts, exceptions, JSON helper, error codes, and response/page DTOs. |
| `web` | WebMVC auto-configuration for `WebContext` building/injection, response wrapping, CORS, and exception handling. HTTP auth interception is application-owned. |
| `mongo` | MongoDB/Morphia datastore auto-configuration, package mapping, index creation, registry, and CRUD base service. |
| `redis` | Redis properties, key helper, `RedisTemplate<String, Object>`, and `StringRedisTemplate`. |
| `grpc` | Maven aggregation module for all gRPC support. |
| `grpc/grpc-common` | Shared gRPC metadata keys, auth context, and string marshaller. |
| `grpc/grpc-server` | gRPC server lifecycle, `@GrpcService` discovery (including per-service `interceptors`), and health/reflection services. Inbound auth interceptors are supplied by the consuming application. |
| `grpc/grpc-client` | gRPC channel factory, `@GrpcClient` field injection, and business-supplied client interceptors. |
| `starter` | Aggregates SDK modules for consumers. |
| `bom` | Dependency management entry point for consumers. |
| `demo` | Local validation app; it must not be deployed. |

## Auto-Configuration Model

Each feature module registers auto-configuration through:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Consumers normally import `sdk-bom` and depend on `sdk-starter`. The BOM contains only `dependencyManagement` entries and adds no runtime code; the starter contains ordinary dependencies that aggregate all SDK feature modules. Consumers can import the BOM and select individual modules when they do not want the full starter. Feature modules are guarded by `@ConditionalOnClass` and `framework.*.enabled` properties where appropriate.

AI model and provider integration deliberately remains the responsibility of consuming applications rather than the framework SDK.

## Auth And Web Flow

`common` defines an extensible, transport-neutral context instead of a fixed auth contract:

- `AtlasContext` is the per-request state shared by HTTP, gRPC and WebSocket, and the common parent of the three transport types. Applications subclass it (or a transport subclass) and register a `WebContextProvider`, then inject their subclass directly into a controller method.
- `AtlasContext` only carries transport basics (`uid`, `ip`, `os`, `language`, `domain`, `session`, expiry, app/os headers, free-form `attributes`). Business fields such as channel, package name, device id, admin flag or permissions are not part of the SDK: a loader puts them on the application subclass or the `attributes` map.
- Each transport ships a base context that extends `AtlasContext` and can be used directly or extended: `WebContext` (HTTP, adds the servlet request/response and is injected into a controller parameter), `GrpcContext` (gRPC, read with `GrpcContext.current()` after a server interceptor attaches it) and `WebsocketContext` (WebSocket, kept on the session attributes and re-bound per message). No transport inherits another, so the shared loaders/holders work against `AtlasContext`.
- `WebContextRequest` abstracts the header/parameter/IP view of an incoming call; `HttpWebContextRequest` (web), `GrpcWebContextRequest` (grpc-common) and the WebSocket adapter all map onto it.
- `WebContextLoader` parses a request into the context (token, tenant, permissions, ...). Loaders are transport-neutral, so one implementation serves every surface. `TokenResolver` is the single token-lookup entry point (header precedence plus optional `Bearer` handling).
- `WebContextAuthorizer` is the business-owned check behind `@AuthRequired(perms = ...)`; when no bean is registered any permission check fails closed.
- `JwtCodec` owns only the JWT crypto (the algorithm is inferred from the supplied HMAC/RSA/EC key) and returns the raw claim map; it never reads a business claim.

Tokens are JWT everywhere, but claim names are never hard-coded. Configure `framework.web.auth.jwt.secret` to let Atlas build a convenience HMAC `JwtCodec` bean and verify the token inside your `WebContextLoader`. There is no `AuthService`/`AuthIdentity`.

The WebMVC request flow is (the SDK ships no HTTP auth interceptor; the application owns it, like it owns gRPC/WebSocket server auth):

1. The application declares a `HandlerInterceptor`. In `preHandle` it calls the SDK's `WebContextFactory`, which builds a `WebContext` (through the registered `WebContextProvider`, or the base type) from request headers and remote IP, runs every `WebContextLoader`, and stores the result in `WebContextHolder`; `afterCompletion` clears it.
2. The same interceptor enforces its own policy. A typical implementation honors `@AuthRequired`: `uid` must be present, the `domain` must match, and any `perms` are delegated to the `WebContextAuthorizer` (absent ⇒ deny). The demo's `DemoWebAuthInterceptor` is the reference.
3. Token parsing, claim names and any auth-only values live entirely in the application's `WebContextLoader`; the SDK never previews a claim.
4. `WebContextCustomizer` beans let consuming applications add business attributes to the HTTP `WebContext`.
5. The resolved context is stored in `WebContextHolder` for synchronous code.
6. `WebContextArgumentResolver` injects any `WebContext` subtype into controller parameters.
7. `WebContextFlux` carries the current `WebContext` into Reactor context for Flux pipelines.
8. `ApiResponseAdvice` wraps JSON/string controller responses into `Response<T>` unless excluded.
9. `GlobalExceptionHandler` converts exceptions into unified `Response` payloads. `AtlasException` is the unified framework exception: `new AtlasException("msg")` answers with `code = -90` (`AtlasException.DEFAULT_CODE` = `ErrorCodes.SYSTEM_ERROR`), an explicit `new AtlasException(code, "msg")` keeps its business code; `IllegalArgumentException` is answered with `404`; every other exception is offered to the registered `ExceptionResponseResolver` beans (which now return an `ExceptionResponse` carrying the HTTP status) and finally answered with `code = -90` instead of a framework error page.
10. `AtlasWebAutoConfiguration` registers the SDK CORS policy from `framework.web.cors.*` as a `HIGHEST_PRECEDENCE` `CorsFilter`, ahead of the auth interceptor, so preflight and error responses are decorated without any application-side CORS bean. When the WebMVC SDK and a gRPC runtime are both present, `sdk-grpc-server`'s `AtlasGrpcWebExceptionAutoConfiguration` adds a downstream gRPC (`StatusRuntimeException`) resolver (`NOT_FOUND → 404`, `UNAVAILABLE`/`DEADLINE_EXCEEDED → 503`, other → `502`, `UNAUTHENTICATED → 401`/`code -94`) (independent of `framework.grpc.server.enabled`).

## gRPC Flow

Server side:

- Services are Spring beans annotated with `@GrpcService` and implementing `BindableService`.
- `GrpcServerLifecycle` starts/stops a Netty gRPC server with discovered services.
- Every `io.grpc.ServerInterceptor` bean is mounted on all services by default, ordered with `AnnotationAwareOrderComparator`. Declare `@GrpcService(interceptors = X.class)` to scope a `ServerInterceptor` bean to one service: the bean is removed from the global set and applied only where it is declared.
- Inbound (server-side) auth is deliberately not shipped by the SDK. The consuming application declares its own `io.grpc.ServerInterceptor` bean(s); `GrpcServerLifecycle` picks up every `ServerInterceptor` bean and chains them. Interceptors read `GrpcMetadataKeys`, run the shared `WebContextLoader` against a `GrpcWebContextRequest`, and attach the result with `GrpcContext.attach`, so a service method reads the same context as HTTP through `GrpcContext.current()`.

Client side:

- `GrpcChannelFactory` creates named channels from `framework.grpc.client.channels.*`.
- `GrpcClientBeanPostProcessor` injects fields annotated with `@GrpcClient`.
- Outbound (client-side) auth is deliberately not shipped by the SDK. Every `ClientInterceptor` bean is collected by `AtlasGrpcClientAutoConfiguration`, ordered with `AnnotationAwareOrderComparator`, and mounted on every channel. The consuming application decides which HTTP header carries the token and which metadata key the callee reads.

Web, gRPC and WebSocket therefore share the `WebContext` type, `WebContextLoader`, `TokenResolver` and `GrpcMetadataKeys`, but never a fixed token-transport policy.

## Data Integrations

MongoDB:

- `framework.mongo.clusters.*` defines multiple independent Mongo clusters. A cluster owns one URI/client and an explicit `datastores` map; it does not create a database or datastore named after the cluster.
- Every `framework.mongo.clusters.<cluster>.datastores.<datastore>` entry defines its own `database` and `map-packages`. Datastore names are global and duplicate names across clusters fail application startup.
- Legacy single-cluster fields (`framework.mongo.uri`, `database`, `map-packages`, `datastores`) still define the default cluster.
- `auto-index` inherits from the global Mongo setting to a cluster and then to each datastore; an explicitly configured child value overrides its parent.
- `MongoClusterRegistry` owns cluster-name to `MongoClient` lookup.
- `MorphiaDatastoreRegistry` owns datastore-name to Morphia `Datastore` lookup and tracks which cluster each datastore belongs to.
- Every configured datastore is also exposed as a Spring bean whose bean name is the datastore name, allowing constructor injection with `@Qualifier("<datastore-name>")`.
- Each datastore maps its entity packages before `ensureIndexes()` is called, so Morphia annotations create or reconcile indexes in the correct database during startup.
- If one entity class is mapped by multiple datastores, callers must use named datastore APIs such as `find(name, type)` or `save(name, entity)`.
- `MongoCrudService<T>` is a base class for common Morphia CRUD operations and can bind to a named datastore by overriding `datastoreName()`.

Redis:

- All standalone Redis connection, timeout, SSL, and pool settings bind from `framework.redis.*`; the SDK creates the `RedisConnectionFactory` directly and does not depend on `spring.data.redis.*`.
- The Redis module uses `spring-data-redis`, `lettuce-core`, and `commons-pool2` directly instead of `spring-boot-starter-data-redis`, so Boot's property-driven Redis auto-configuration is not present on the SDK classpath.
- `AtlasRedisAutoConfiguration` creates `redisTemplateWithObject`, `redisTemplate`, and `redisKey`.
- `AtlasRedisKey` centralizes key construction.

## Demo Validation

The demo app validates:

- WebContext argument injection.
- `WebContextCustomizer` business extension attributes.
- `@AuthRequired` domain/permission checks through the business-owned `DemoWebAuthInterceptor`.
- Unified response wrapping.
- gRPC server/client integration.
- Business-owned gRPC auth in both directions: the demo declares its own server and client interceptors.
- Redis basic set/get/delete operations.
- MongoDB basic save/find/delete operations against a named cluster datastore.

### Multi-cluster Mongo example

`demo/src/main/resources/application.yaml` defines the following topology:

| cluster | datastore | database | mapped collections |
| --- | --- | --- | --- |
| `primary` | `catalog` | `framework_demo_catalog` | `catalog_products`, `catalog_categories` |
| `primary` | `audit` | `framework_demo_audit` | `audit_events`, `login_records` |
| `secondary` | `sales` | `framework_demo_sales` | `sales_orders`, `customer_profiles` |
| `secondary` | `archive` | `framework_demo_archive` | `archived_orders`, `archive_jobs` |

Run it with:

```bash
MONGO_PRIMARY_URI='mongodb://127.0.0.1:27017' \
MONGO_SECONDARY_URI='mongodb://127.0.0.1:27018' \
FRAMEWORK_MONGO_ENABLED=true mvn -pl demo -am spring-boot:run
```

The datastore names are global routing keys and must be unique across all clusters. Every datastore is explicitly nested under the cluster whose `MongoClient` it shares. The four demo services (`CatalogProductService`, `AuditEventService`, `SalesOrderService`, and `ArchivedOrderService`) demonstrate direct constructor injection, for example:

```java
public AuditEventService(@Qualifier("audit") Datastore datastore) {
    this.datastore = datastore;
}
```

Index creation resolves configuration in this order:

```text
framework.mongo.auto-index
  -> framework.mongo.clusters.<cluster>.auto-index
    -> framework.mongo.clusters.<cluster>.datastores.<datastore>.auto-index
```

After package mapping, startup calls Morphia `Datastore.ensureIndexes()` once for each datastore whose resolved value is `true`. The demo entities include unique, compound, descending, and TTL index annotations. Setting a datastore to `false` skips only that database's index synchronization.

The demo module is local validation only:

- `demo/pom.xml` sets `<maven.deploy.skip>true</maven.deploy.skip>`.
- `deploy.sh` runs `mvn -pl '!demo' clean deploy -DskipTests`.

## Build And Release

- Use Java 17 for all verification and release commands.
- Published release versions are immutable in `hero-releases`; `./deploy.sh` increments the root `<revision>` before publishing.
- `./build.sh` switches to Java 17 and runs `mvn -Phero-rdc clean package -DskipTests`.
- `./build.sh --help` prints usage and exits without building.
- Full validation should run `mvn test` with Java 17 before release.
- `./deploy.sh` switches to Java 17, increments `_vNNN`, and deploys all publishable modules with `mvn -Phero-rdc -pl '!demo' clean deploy -DskipTests`.
- `./deploy.sh --help` prints usage and exits without deploying.
