# Releases

Published to [Maven Central](https://central.sonatype.com/namespace/io.github.doriangrelu)
under `io.github.doriangrelu`. Every release is a git tag `vX.Y.Z` cut from `main` (see
[CONTRIBUTING.md](CONTRIBUTING.md) §5). While `0.x`, any release may contain breaking
changes.

| Version | Date | Stack | Highlights |
|---|---|---|---|
| [`0.1.0`](https://central.sonatype.com/artifact/io.github.doriangrelu/openapi-contract-generator/0.1.0) | 2026-09-10 | Spring Boot 4.1 · springdoc 3.1 · Java 25 | First release. `@ApiContract` / `@ApiDomain` markers; one OpenAPI 3.1 document per bounded context; server-less generation driven by `OpenApiSpecGeneratorTest`; specialised internal packages; unit tests. |

## Coordinates

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

## Links

- Central namespace — <https://central.sonatype.com/namespace/io.github.doriangrelu>
- Javadoc — <https://javadoc.io/doc/io.github.doriangrelu/openapi-contract-generator>
- Tags — <https://github.com/doriangrelu/openapi-contract-generator/tags>
