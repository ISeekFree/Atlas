# sdk-webmvc

Spring Boot 4.x SDK for WebMVC services. It provides reusable auto-configuration for:

- WebMVC response wrapping, `WebContext`, `@AuthRequired`, and unified auth extension points.
- Morphia-style MongoDB datastores with package mapping and index creation.
- Redis `RedisTemplate` and `StringRedisTemplate`.
- gRPC server/client integration with shared Web/gRPC auth context propagation.

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

Override `AuthService` in the consuming app to connect `@AuthRequired`, WebContext, and gRPC auth to the real session/account system.

## gRPC Client Targets

Atlas registers `static://host:port` for a fixed address and makes `static` the default resolver. A target without a scheme, such as `127.0.0.1:19090` or `account.internal:19090`, is therefore treated as static; IPv4, domain names, and bracketed IPv6 are supported. The SDK includes the official `dns`, `xds`, and Netty `unix` providers. Service discovery uses an explicit scheme: `dns:///service:port` for DNS or Kubernetes Services, `xds:///service-name` with the xDS runtime/bootstrap, or `unix:///path/to.sock` for Unix-domain sockets when native transport support is available.

## Extension Points

- Register a `WebContextCustomizer` bean to add business attributes to `WebContext`.
- Configure MongoDB with `framework.mongo.clusters.*` for multiple independent Mongo clusters. Define all databases and mapped packages under each cluster's explicit `datastores` map; datastore names must be globally unique.

The runnable multi-cluster Mongo example is in `demo/src/main/resources/application.yaml`. It contains two clusters, two databases per cluster, two collections per database, automatic unique/compound/TTL index examples, and qualified `Datastore` injection into separate services.
