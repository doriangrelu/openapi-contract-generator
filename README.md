# openapi-contract-generator

[![Maven Central](https://img.shields.io/maven-central/v/io.github.doriangrelu/openapi-contract-generator)](https://central.sonatype.com/namespace/io.github.doriangrelu)
[![build](https://github.com/doriangrelu/openapi-contract-generator/actions/workflows/build.yml/badge.svg)](https://github.com/doriangrelu/openapi-contract-generator/actions/workflows/build.yml)

Generate **one OpenAPI 3.1 document per bounded context** from annotated Java API
interfaces, **at build time, without starting an HTTP server**.

Your API — routes, payload shapes, domain boundaries and documentation metadata — lives
entirely in Java, next to the code, as annotations. `mvn test` turns it into
`billing.yaml`, `catalog.yaml`, … one file per domain.

Under the hood it reuses **springdoc**'s Spring MVC → OpenAPI engine (mature, maintained)
rather than re-implementing it. The cost — a Spring context during tests, and something for
Spring MVC to map — is fully absorbed by this library: a contract module writes no
controller, no application class and no test body.

---

## Modules

| Module | Published | Purpose |
|---|---|---|
| `openapi-contract-annotations` | yes | The two marker annotations. The **only compile-scope** dependency a contract module needs. |
| `openapi-contract-generator` | yes | Server-less Spring context + per-domain proxy registration + `GroupedOpenApi` wiring + the base test. Consumed in **test scope**. |
| `openapi-contract-example` | no | Two bounded contexts (`billing`, `catalog`) + a shared kernel. The complete consumer setup. |

---

## Concepts

### `@ApiContract` — on an interface

"This interface is a set of HTTP endpoints." Meta-annotated with `@RestController` +
`@RequestMapping`, so a documentation-only proxy of it is treated by Spring MVC and
springdoc exactly like a hand-written controller.

```java
@ApiContract("/invoices")
@Tag(name = "Invoices")
public interface InvoiceApi {

    @Operation(summary = "Get an invoice by id")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{invoiceId}")
    Invoice getInvoice(@PathVariable("invoiceId") String invoiceId);
}
```

### `@ApiDomain` — on a `package-info.java`

"This package is a bounded context that owns one OpenAPI document." It carries an `id`
(the file name / springdoc group) and reuses the swagger-core annotation vocabulary for
metadata.

```java
@ApiDomain(
        id = "billing",
        info = @Info(title = "Billing API", version = "1.4.0"),
        servers = @Server(url = "https://billing.example.com"),
        securitySchemes = @SecurityScheme(
                name = "bearerAuth", type = SecuritySchemeType.HTTP,
                scheme = "bearer", bearerFormat = "JWT"))
package com.example.billing;
```

Every `@ApiContract` interface is attributed to its **nearest `@ApiDomain` ancestor
package**. Shared DTOs may live in any package (no `@ApiDomain` needed) and are resolved
independently into each document that references them.

### Rules enforced at build time (fail the build)

- `id` matches `^[a-z0-9][a-z0-9-]*$`.
- `id`s are unique.
- A `@ApiDomain` package may **not** be nested inside another `@ApiDomain` package.
- Every `@ApiContract` interface has **exactly one** `@ApiDomain` ancestor (orphans fail).

---

## Consumer setup

Three things, no build-plugin configuration:

**1. Depend on the annotations (compile) and the generator (test):**

```xml
<dependency>
  <groupId>io.github.doriangrelu</groupId>
  <artifactId>openapi-contract-annotations</artifactId>
  <version>0.1.0</version>
</dependency>
<dependency>
  <groupId>io.github.doriangrelu</groupId>
  <artifactId>openapi-contract-generator</artifactId>
  <version>0.1.0</version>
  <scope>test</scope>
</dependency>
```

Latest version and history: [docs/RELEASES.md](docs/RELEASES.md).

**2. Annotate:** `@ApiContract` on each interface, one `@ApiDomain` `package-info.java` per
bounded context.

**3. Add one empty test class:**

```java
class GenerateOpenApiSpecs extends io.github.doriangrelu.generator.OpenApiSpecGeneratorTest {
}
```

Then:

```bash
mvn test
```

Output (default `target/openapi/`, override with `-Dopenapi.output.dir=...`):

```
target/openapi/billing.json
target/openapi/billing.yaml
target/openapi/catalog.json
target/openapi/catalog.yaml
```

---

## How it works

1. `GenerateOpenApiSpecs` starts `OpenApiGeneratorApplication` with
   `WebEnvironment.MOCK` — a Spring context, **no port**.
2. `OpenApiGeneratorRegistrar` (a ClassGraph scan during context init):
   - finds every `@ApiContract` interface and every `@ApiDomain` package;
   - validates the rules above;
   - registers one **JDK proxy bean** per interface (Spring MVC supports
     `@RequestMapping` on an interface when the bean is an interface-based proxy);
   - registers one `GroupedOpenApi` per domain, with a method filter keyed on the
     declaring interface and a customizer that injects `info` / `servers` /
     `securitySchemes` translated from `@ApiDomain`.
3. The base test calls `GET /v3/api-docs/{group}` through `MockMvc`, writes `<id>.json`,
   and re-serialises it to `<id>.yaml` with swagger-core's `Yaml31`.
4. It asserts each file was produced (HTTP 200, non-empty). It does **not** inspect the
   content — golden-file / lint / breaking-change checks are the consumer's to add.

### Why not `springdoc-openapi-maven-plugin`?

That plugin drives `spring-boot-maven-plugin` (`start` → `GET localhost:8080/v3/api-docs`
→ `stop`): the real application must boot (datasource, full config, third-party services).
Here `WebEnvironment.MOCK` starts a context with no server, and since a contract module is
only interfaces + DTOs, that context is trivial.

---

## Stack

Verified on Maven Central, September 2026.

| Component | Version | Note |
|---|---|---|
| Spring Boot | `4.1.1` | BOM imported; modules do **not** inherit `spring-boot-starter-parent` |
| springdoc-openapi | `3.1.1` | Spring Boot 3.x/4.x, OpenAPI 3.1; needs Java 21+ |
| swagger-core | `2.2.54` | annotation vocabulary + `Json31` / `Yaml31` |
| ClassGraph | `4.8.194` | classpath scan for the markers |
| Java | `25` | `maven.compiler.release` |

Equivalent Spring Boot 3.x stack if needed: Spring Boot `3.5.x` + springdoc `2.9.x` +
Java 17.

---

## Known limitations

- **Interface-based proxy.** Give `@RequestParam` / `@PathVariable` an explicit `name`;
  parameter-name inference (`-parameters`) does not survive the proxy.
- **Spring MVC only** — no WebFlux (`Mono` / `Flux` are not unwrapped).
- **Whole-classpath scan** for the markers, minus a short infrastructure reject list. Fast
  in practice; the markers are unique to this library so there is no ambiguity.
- **Schema names** are simple class names (springdoc / swagger-core behaviour): two
  same-named DTOs in different packages collide within one document.
- **Security-scheme translation** covers the common fields and OAuth flows; vendor
  extensions are not propagated yet.

---

## Roadmap

A future **Maven plugin** could read the interfaces by reflection over the compiled
classes, reuse swagger-core's `ModelConverters` for schemas, and port the needed subset of
springdoc's Spring MVC interpretation — no Spring context, no proxy. The documents produced
here would then serve as the golden files validating that plugin.

---

## Contributing

Coding standards (`final`, immutability, streams, null handling, package structure),
testing conventions and the release process are documented in
[docs/CONTRIBUTING.md](docs/CONTRIBUTING.md).

---

## Releasing (maintainers)

Published versions and coordinates: [docs/RELEASES.md](docs/RELEASES.md).

Trunk-based: `main` stays on `X.Y.Z-SNAPSHOT`; the release version is stamped from the git
tag. Artifacts go to **Maven Central** via the Central Portal, namespace
`io.github.doriangrelu`. The two library modules are released; `openapi-contract-example`
is not.

```bash
scripts/release.sh 0.1.0
```

The script verifies the build, tags `vX.Y.Z` and pushes it, then bumps `main` to the next
`-SNAPSHOT`. The tag triggers [`.github/workflows/release.yml`](.github/workflows/release.yml)
which builds, GPG-signs and uploads a bundle; with `autoPublish=false` it is released with
one click from
[central.sonatype.com](https://central.sonatype.com/publishing/deployments).

Secrets live in the **`prod` GitHub Environment** (Settings → Environments → prod):
`CENTRAL_TOKEN_USERNAME`, `CENTRAL_TOKEN_PASSWORD`, `GPG_PRIVATE_KEY` (armored *private*
key), `GPG_PASSPHRASE`. Details and toggles in
[docs/CONTRIBUTING.md](docs/CONTRIBUTING.md).

---

## Acknowledgements

Thanks to [@ramziGY](https://github.com/ramziGY) for helping think through the design of
this solution.

---

## License

Apache License 2.0 — see [LICENSE](LICENSE) and [NOTICE](NOTICE).
Copyright 2026 Dorian Grelu.
