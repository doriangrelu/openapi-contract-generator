package io.github.doriangrelu.generator;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Minimal, server-less Spring Boot configuration used by {@link OpenApiSpecGeneratorTest}.
 *
 * <p>It is deliberately shipped in {@code src/main}: this module is a test-support library,
 * comparable to {@code org.testcontainers:junit-jupiter}. {@link OpenApiGeneratorRegistrar}
 * contributes the per-domain proxy and
 * {@link org.springdoc.core.models.GroupedOpenApi GroupedOpenApi} beans; the rest is
 * Spring Boot auto-configuration for Spring MVC and springdoc.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(OpenApiGeneratorRegistrar.class)
public class OpenApiGeneratorApplication {
}
