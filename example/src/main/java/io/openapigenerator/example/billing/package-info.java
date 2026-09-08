/**
 * Billing bounded context: invoices and their lifecycle.
 *
 * <p>Produces {@code billing.json} / {@code billing.yaml}. Declares a bearer-JWT security
 * scheme, unlike the catalog domain.
 */
@ApiDomain(
        id = "billing",
        info = @Info(
                title = "Billing API",
                version = "1.4.0",
                description = "Invoices and payment lifecycle for the billing bounded context.",
                contact = @Contact(name = "Billing Team", email = "billing@example.com"),
                license = @License(name = "Apache-2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")),
        servers = {
                @Server(url = "https://billing.example.com", description = "Production"),
                @Server(url = "https://billing.staging.example.com", description = "Staging")
        },
        securitySchemes = @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                description = "JWT access token issued by the identity provider"))
package io.openapigenerator.example.billing;

import io.openapigenerator.contract.ApiDomain;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
