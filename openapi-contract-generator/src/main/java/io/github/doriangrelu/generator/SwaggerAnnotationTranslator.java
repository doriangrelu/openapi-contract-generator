package io.github.doriangrelu.generator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

/**
 * Translates the swagger-core <em>annotation</em> types reused as members of
 * {@link io.github.doriangrelu.contract.ApiDomain @ApiDomain} into their
 * {@code io.swagger.v3.oas.models} counterparts.
 *
 * <p>Only fields meaningful for a build-time contract document are copied; a blank string
 * is treated as "unset". Vendor extensions are not propagated.
 */
final class SwaggerAnnotationTranslator {

    private static final String DEFAULT_VERSION = "0.0.1";

    private SwaggerAnnotationTranslator() {
    }

    /**
     * @param source        the {@code @Info} annotation from {@code @ApiDomain}
     * @param fallbackTitle used as {@code info.title} when the annotation leaves it blank
     * @return the model {@link Info} (never {@code null})
     */
    static Info toInfo(io.swagger.v3.oas.annotations.info.Info source, String fallbackTitle) {
        Info info = new Info()
                .title(hasText(source.title()) ? source.title() : fallbackTitle)
                .version(hasText(source.version()) ? source.version() : DEFAULT_VERSION)
                .description(orNull(source.description()))
                .termsOfService(orNull(source.termsOfService()));

        io.swagger.v3.oas.annotations.info.Contact contact = source.contact();
        if (hasText(contact.name()) || hasText(contact.url()) || hasText(contact.email())) {
            info.contact(new Contact()
                    .name(orNull(contact.name()))
                    .url(orNull(contact.url()))
                    .email(orNull(contact.email())));
        }

        io.swagger.v3.oas.annotations.info.License license = source.license();
        if (hasText(license.name()) || hasText(license.url()) || hasText(license.identifier())) {
            License model = new License().name(orNull(license.name())).url(orNull(license.url()));
            if (hasText(license.identifier())) {
                model.identifier(license.identifier());
            }
            info.license(model);
        }
        return info;
    }

    /**
     * @param sources the {@code @Server} annotations from {@code @ApiDomain}
     * @return the model server list (never {@code null}, possibly empty)
     */
    static List<Server> toServers(io.swagger.v3.oas.annotations.servers.Server[] sources) {
        return Arrays.stream(sources).map(SwaggerAnnotationTranslator::toServer).toList();
    }

    private static Server toServer(io.swagger.v3.oas.annotations.servers.Server source) {
        Server server = new Server().url(source.url()).description(orNull(source.description()));
        if (source.variables().length > 0) {
            ServerVariables variables = new ServerVariables();
            for (io.swagger.v3.oas.annotations.servers.ServerVariable source0 : source.variables()) {
                ServerVariable variable = new ServerVariable()
                        ._default(source0.defaultValue())
                        .description(orNull(source0.description()));
                if (source0.allowableValues().length > 0) {
                    variable.setEnum(List.of(source0.allowableValues()));
                }
                variables.addServerVariable(source0.name(), variable);
            }
            server.variables(variables);
        }
        return server;
    }

    /**
     * @param sources the {@code @SecurityScheme} annotations from {@code @ApiDomain}
     * @return a map keyed by {@code @SecurityScheme.name()} for
     *         {@code components.securitySchemes}
     */
    static Map<String, SecurityScheme> toSecuritySchemes(
            io.swagger.v3.oas.annotations.security.SecurityScheme[] sources) {
        Map<String, SecurityScheme> schemes = new LinkedHashMap<>();
        for (io.swagger.v3.oas.annotations.security.SecurityScheme source : sources) {
            if (!hasText(source.name())) {
                throw new ApiContractConfigurationException(
                        "@SecurityScheme.name (the key under components.securitySchemes) must be set");
            }
            schemes.put(source.name(), toSecurityScheme(source));
        }
        return schemes;
    }

    private static SecurityScheme toSecurityScheme(
            io.swagger.v3.oas.annotations.security.SecurityScheme source) {
        if ("DEFAULT".equals(source.type().name())) {
            throw new ApiContractConfigurationException(
                    "@SecurityScheme(name=\"" + source.name() + "\") must declare a type");
        }
        SecurityScheme scheme = new SecurityScheme()
                .type(SecurityScheme.Type.valueOf(source.type().name()))
                .description(orNull(source.description()))
                .name(orNull(source.paramName()))
                .$ref(orNull(source.ref()));
        if (!"DEFAULT".equals(source.in().name())) {
            scheme.setIn(SecurityScheme.In.valueOf(source.in().name()));
        }
        if (hasText(source.scheme())) {
            scheme.setScheme(source.scheme());
        }
        if (hasText(source.bearerFormat())) {
            scheme.setBearerFormat(source.bearerFormat());
        }
        if (hasText(source.openIdConnectUrl())) {
            scheme.setOpenIdConnectUrl(source.openIdConnectUrl());
        }
        OAuthFlows flows = toFlows(source.flows());
        if (flows != null) {
            scheme.setFlows(flows);
        }
        return scheme;
    }

    private static OAuthFlows toFlows(io.swagger.v3.oas.annotations.security.OAuthFlows source) {
        OAuthFlows flows = new OAuthFlows();
        boolean any = false;
        OAuthFlow implicit = toFlow(source.implicit());
        if (implicit != null) {
            flows.setImplicit(implicit);
            any = true;
        }
        OAuthFlow password = toFlow(source.password());
        if (password != null) {
            flows.setPassword(password);
            any = true;
        }
        OAuthFlow clientCredentials = toFlow(source.clientCredentials());
        if (clientCredentials != null) {
            flows.setClientCredentials(clientCredentials);
            any = true;
        }
        OAuthFlow authorizationCode = toFlow(source.authorizationCode());
        if (authorizationCode != null) {
            flows.setAuthorizationCode(authorizationCode);
            any = true;
        }
        return any ? flows : null;
    }

    private static OAuthFlow toFlow(io.swagger.v3.oas.annotations.security.OAuthFlow source) {
        boolean present = hasText(source.authorizationUrl()) || hasText(source.tokenUrl())
                || hasText(source.refreshUrl()) || source.scopes().length > 0;
        if (!present) {
            return null;
        }
        OAuthFlow flow = new OAuthFlow()
                .authorizationUrl(orNull(source.authorizationUrl()))
                .tokenUrl(orNull(source.tokenUrl()))
                .refreshUrl(orNull(source.refreshUrl()));
        if (source.scopes().length > 0) {
            Scopes scopes = new Scopes();
            for (io.swagger.v3.oas.annotations.security.OAuthScope scope : source.scopes()) {
                scopes.addString(scope.name(), scope.description());
            }
            flow.setScopes(scopes);
        }
        return flow;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String orNull(String value) {
        return hasText(value) ? value : null;
    }
}
