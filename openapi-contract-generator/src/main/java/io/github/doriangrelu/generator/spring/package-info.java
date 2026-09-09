/**
 * Server-less Spring Boot wiring: the {@code @SpringBootConfiguration} used by the base
 * test and the {@code ImportBeanDefinitionRegistrar} that contributes the proxy and
 * {@code GroupedOpenApi} beans.
 *
 * <p>Internal API — {@code OpenApiGeneratorApplication} is referenced by
 * {@code @SpringBootTest(classes = ...)} but is otherwise not part of the supported
 * surface; may change without notice.
 */
package io.github.doriangrelu.generator.spring;
