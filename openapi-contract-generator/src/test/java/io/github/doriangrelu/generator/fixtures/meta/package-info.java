@ApiDomain(
        id = "meta",
        info = @Info(
                title = "Meta API",
                version = "3.2.1",
                description = "rich info fixture",
                termsOfService = "https://meta.example.com/tos",
                contact = @Contact(name = "Meta Team", url = "https://meta.example.com", email = "meta@example.com"),
                license = @License(name = "Apache-2.0", url = "https://www.apache.org/licenses/LICENSE-2.0",
                        identifier = "Apache-2.0")),
        servers = {
                @Server(url = "https://{region}.meta.example.com", description = "regional",
                        variables = @ServerVariable(name = "region", defaultValue = "eu",
                                allowableValues = {"eu", "us"}, description = "deployment region"))
        },
        securitySchemes = {
                @SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
                        scheme = "bearer", bearerFormat = "JWT", description = "JWT bearer"),
                @SecurityScheme(name = "apiKey", type = SecuritySchemeType.APIKEY,
                        in = SecuritySchemeIn.HEADER, paramName = "X-API-Key"),
                @SecurityScheme(name = "oauth", type = SecuritySchemeType.OAUTH2,
                        flows = @OAuthFlows(authorizationCode = @OAuthFlow(
                                authorizationUrl = "https://auth.example.com/authorize",
                                tokenUrl = "https://auth.example.com/token",
                                scopes = {
                                        @OAuthScope(name = "read", description = "read access"),
                                        @OAuthScope(name = "write", description = "write access")
                                })))
        })
package io.github.doriangrelu.generator.fixtures.meta;

import io.github.doriangrelu.contract.ApiDomain;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
