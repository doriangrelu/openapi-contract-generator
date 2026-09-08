/**
 * Build-time OpenAPI 3.1 generation for {@code @ApiContract} interfaces, grouped by
 * {@code @ApiDomain} bounded context.
 *
 * <p><b>Consumer entry point:</b> extend
 * {@link io.openapigenerator.generator.OpenApiSpecGeneratorTest} with an empty subclass in
 * {@code src/test/java}. Everything else in this package is internal machinery:
 *
 * <ul>
 *   <li>{@link io.openapigenerator.generator.ContractScanner} — ClassGraph scan for the
 *       marker annotations;</li>
 *   <li>{@link io.openapigenerator.generator.ApiDomainResolver} — validation and
 *       interface-to-domain attribution;</li>
 *   <li>{@link io.openapigenerator.generator.ContractProxyFactory} — documentation-only
 *       JDK proxies;</li>
 *   <li>{@link io.openapigenerator.generator.OpenApiGeneratorRegistrar} — registers the
 *       proxy and {@code GroupedOpenApi} beans;</li>
 *   <li>{@link io.openapigenerator.generator.SwaggerAnnotationTranslator} — swagger
 *       annotation types to {@code oas.models} objects.</li>
 * </ul>
 */
package io.openapigenerator.generator;
