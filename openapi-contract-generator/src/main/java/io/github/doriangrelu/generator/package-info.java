/**
 * Build-time OpenAPI 3.1 generation for {@code @ApiContract} interfaces, grouped by
 * {@code @ApiDomain} bounded context.
 *
 * <h2>Public surface</h2>
 * Consumers use exactly two types from this module:
 * <ul>
 *   <li>{@link io.github.doriangrelu.generator.OpenApiSpecGeneratorTest} — extend it with an
 *       empty subclass in {@code src/test/java} to enable generation;</li>
 *   <li>{@link io.github.doriangrelu.generator.ApiContractConfigurationException} — thrown
 *       when the {@code @ApiContract} / {@code @ApiDomain} declarations are inconsistent.</li>
 * </ul>
 *
 * <h2>Internal packages</h2>
 * Everything under {@code io.github.doriangrelu.generator.*} is internal wiring and may
 * change without notice:
 * <ul>
 *   <li>{@code .scan} — ClassGraph discovery of the marker annotations;</li>
 *   <li>{@code .domain} — validation and interface-to-domain attribution;</li>
 *   <li>{@code .proxy} — documentation-only JDK proxies;</li>
 *   <li>{@code .openapi} — swagger annotation to {@code oas.models} mapping;</li>
 *   <li>{@code .spring} — the server-less Spring Boot wiring.</li>
 * </ul>
 */
package io.github.doriangrelu.generator;
