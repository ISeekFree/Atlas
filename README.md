# claw-sdk-webmvc

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

`claw-sdk-bom` only manages compatible module versions; it adds no runtime dependencies. `claw-sdk-starter` is the convenience dependency that actually brings in all SDK feature modules. Consumers that need only selected features can import the BOM and depend on individual modules instead of the starter.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.iseekfree.common</groupId>
      <artifactId>claw-sdk-bom</artifactId>
      <version>4.1.0_v008</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.iseekfree.common</groupId>
    <artifactId>claw-sdk-starter</artifactId>
  </dependency>
</dependencies>
```

Override `AuthService` in the consuming app to connect `@AuthRequired`, WebContext, and gRPC auth to the real session/account system.

## Extension Points

- Register a `WebContextCustomizer` bean to add business attributes to `WebContext`.
- Configure MongoDB with `claw.mongo.clusters.*` for multiple independent Mongo clusters. Define all databases and mapped packages under each cluster's explicit `datastores` map; datastore names must be globally unique.

The runnable multi-cluster Mongo example is in `demo/src/main/resources/application.yaml`. It contains two clusters, two databases per cluster, two collections per database, automatic unique/compound/TTL index examples, and qualified `Datastore` injection into separate services.
