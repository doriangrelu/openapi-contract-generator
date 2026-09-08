/**
 * Catalog bounded context: the product catalogue.
 *
 * <p>Produces {@code catalog.json} / {@code catalog.yaml}. A different version and server
 * set than the billing domain, and no security scheme.
 */
@ApiDomain(
        id = "catalog",
        info = @Info(
                title = "Catalog API",
                version = "2.1.0",
                description = "Products and categories for the catalog bounded context."),
        servers = @Server(url = "https://catalog.example.com", description = "Production"))
package io.openapigenerator.example.catalog;

import io.openapigenerator.contract.ApiDomain;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
