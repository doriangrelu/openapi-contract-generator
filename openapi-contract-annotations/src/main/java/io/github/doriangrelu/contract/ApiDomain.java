package io.github.doriangrelu.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Declares a package as the root of a <em>bounded context</em> that owns one dedicated
 * OpenAPI document.
 *
 * <p>Placed on a {@code package-info.java} file:
 * <pre>{@code
 * @ApiDomain(
 *         id = "billing",
 *         info = @Info(title = "Billing API", version = "1.4.0"),
 *         servers = @Server(url = "https://billing.example.com"),
 *         securitySchemes = @SecurityScheme(
 *                 name = "bearerAuth", type = SecuritySchemeType.HTTP,
 *                 scheme = "bearer", bearerFormat = "JWT"))
 * package com.example.billing;
 * }</pre>
 *
 * <p>Every {@link ApiContract @ApiContract} interface located in this package or a
 * sub-package is attributed to this domain (nearest {@code @ApiDomain} ancestor wins).
 * The generator produces {@code <id>.json} and {@code <id>.yaml}.
 *
 * <h2>Rules enforced at generation time</h2>
 * <ul>
 *   <li>{@link #id()} must match {@code ^[a-z0-9][a-z0-9-]*$} (URL- and file-name-safe).</li>
 *   <li>Two {@code @ApiDomain} packages may not share the same {@code id}.</li>
 *   <li>A {@code @ApiDomain} package may not be nested inside another {@code @ApiDomain}
 *       package.</li>
 *   <li>Every {@code @ApiContract} interface must have exactly one {@code @ApiDomain}
 *       ancestor package.</li>
 * </ul>
 * Any violation fails the build.
 *
 * <p>The {@link #info()}, {@link #servers()} and {@link #securitySchemes()} members reuse
 * the swagger-core annotation types verbatim; they are translated into the corresponding
 * {@code io.swagger.v3.oas.models} objects and merged into the domain document.
 *
 * @see ApiContract
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDomain {

    /**
     * Stable identifier of the domain. Used as the springdoc group name and as the
     * generated file base name. Must match {@code ^[a-z0-9][a-z0-9-]*$}.
     *
     * @return the domain id
     */
    String id();

    /**
     * OpenAPI {@code info} object for this domain. When {@link Info#title()} is blank the
     * {@link #id()} is used as the title.
     *
     * @return the info annotation (defaults to an empty {@code @Info})
     */
    Info info() default @Info();

    /**
     * Servers advertised by this domain's document.
     *
     * @return zero or more server declarations
     */
    Server[] servers() default {};

    /**
     * Security schemes published under {@code components.securitySchemes} of this domain's
     * document.
     *
     * @return zero or more security scheme declarations
     */
    SecurityScheme[] securitySchemes() default {};
}
