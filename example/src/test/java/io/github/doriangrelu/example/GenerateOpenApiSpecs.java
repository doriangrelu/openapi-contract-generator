package io.github.doriangrelu.example;

import io.github.doriangrelu.generator.OpenApiSpecGeneratorTest;

/**
 * Enables OpenAPI generation for this contract module. This empty subclass is the entire
 * consumer footprint: {@code mvn test} writes {@code target/openapi/billing.{json,yaml}}
 * and {@code target/openapi/catalog.{json,yaml}}.
 *
 * @see OpenApiSpecGeneratorTest
 */
class GenerateOpenApiSpecs extends OpenApiSpecGeneratorTest {
}
