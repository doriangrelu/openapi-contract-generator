package io.openapigenerator.example;

import io.openapigenerator.generator.OpenApiSpecGeneratorTest;

/**
 * Enables OpenAPI generation for this contract module. This empty subclass is the entire
 * consumer footprint: {@code mvn test} writes {@code target/openapi/billing.{json,yaml}}
 * and {@code target/openapi/catalog.{json,yaml}}.
 *
 * @see OpenApiSpecGeneratorTest
 */
class GenerateOpenApiSpecs extends OpenApiSpecGeneratorTest {
}
