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
| `common` | Shared auth contracts, exceptions, JSON helper, and response/page DTOs. |
| `web` | WebMVC auto-configuration for `WebContext`, auth interception, response wrapping, and exception handling. |
| `mongo` | MongoDB/Morphia datastore auto-configuration, package mapping, index creation, registry, and CRUD base service. |
| `redis` | Redis properties, key helper, `RedisTemplate<String, Object>`, and `StringRedisTemplate`. |
| `grpc` | Maven aggregation module for all gRPC support. |
| `grpc/grpc-common` | Shared gRPC metadata keys, auth context, and string marshaller. |
| `grpc/grpc-server` | gRPC server lifecycle, `@GrpcService` discovery, health/reflection services, and server auth interceptor. |
| `grpc/grpc-client` | gRPC channel factory, `@GrpcClient` field injection, and client auth metadata propagation. |
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

`common` defines the auth boundary:

- `AuthService` verifies an `AuthRequest` and returns an `AuthIdentity`.
- `CookieStyleAuthService` is a default demo-friendly implementation. Production apps should override `AuthService`.
- `AuthIdentity` is shared by WebMVC and gRPC, but is not stored in `WebContext`.

The WebMVC request flow is:

1. `WebAuthInterceptor` builds a `WebContext` from request headers and remote IP.
2. If `@AuthRequired` or `framework.web.auth.required-by-default` applies, it calls `AuthService`.
3. Auth-only values such as token, admin flag, channel, and `AuthIdentity` are not fields on `WebContext`.
4. The resolved user-visible fields are written back to `WebContext`.
5. `WebContextCustomizer` beans let consuming applications add business attributes to `WebContext`.
6. The resolved context is stored in `WebContextHolder` for synchronous code.
7. `WebContextArgumentResolver` injects `WebContext` controller parameters.
8. `WebContextFlux` carries the current `WebContext` into Reactor context for Flux pipelines.
9. `ApiResponseAdvice` wraps JSON/string controller responses into `Response<T>` unless excluded.
10. `GlobalExceptionHandler` converts SDK exceptions into unified `Response` payloads.

## gRPC Flow

Server side:

- Services are Spring beans annotated with `@GrpcService` and implementing `BindableService`.
- `GrpcServerLifecycle` starts/stops a Netty gRPC server with discovered services.
- `GrpcAuthServerInterceptor` reads auth metadata, delegates to `AuthService`, and writes the identity to `GrpcAuthContext`.

Client side:

- `GrpcChannelFactory` creates named channels from `framework.grpc.client.channels.*`.
- `GrpcClientBeanPostProcessor` injects fields annotated with `@GrpcClient`.
- `GrpcClientAuthInterceptor` reads `WebContextHolder` and forwards token/admin-token metadata to downstream gRPC calls.

This gives one auth contract across WebMVC controllers and gRPC services.

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
- `@AuthRequired` domain/permission checks.
- Unified response wrapping.
- gRPC server/client integration.
- Web auth metadata propagation into gRPC auth context.
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
