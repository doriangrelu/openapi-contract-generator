/**
 * Marker annotations for describing HTTP APIs as plain Java interfaces.
 *
 * <ul>
 *   <li>{@link io.openapigenerator.contract.ApiContract @ApiContract} — on an interface,
 *       "this is a set of HTTP endpoints".</li>
 *   <li>{@link io.openapigenerator.contract.ApiDomain @ApiDomain} — on a
 *       {@code package-info}, "this package is a bounded context with its own OpenAPI
 *       document".</li>
 * </ul>
 *
 * <p>This module carries no logic; it is the only compile-scope dependency an API contract
 * module needs. Specification generation is performed by
 * {@code io.openapigenerator:openapi-contract-generator} (test scope).
 */
package io.openapigenerator.contract;
