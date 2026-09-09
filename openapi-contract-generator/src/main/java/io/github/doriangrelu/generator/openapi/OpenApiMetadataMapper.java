package io.github.doriangrelu.generator.openapi;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.doriangrelu.generator.ApiContractConfigurationException;
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
 * Maps the swagger-core <em>annotation</em> types reused as members of
 * {@link io.github.doriangrelu.contract.ApiDomain @ApiDomain} onto their
 * {@code io.swagger.v3.oas.models} counterparts.
 *
 * <p>Only fields meaningful for a build-time contract document are copied; a blank string
 * is treated as "unset". Vendor extensions are not propagated.
 *
 * <p>Internal API.
 */
public final class OpenApiMetadataMapper {

    private static final String DEFAULT_VERSION = "0.0.1";
    private static final String DEFAULT_ENUM = "DEFAULT";

    private OpenApiMetadataMapper() {
    }

    /**
     * @param source        the {@code @Info} annotation from {@code @ApiDomain}
     * @param fallbackTitle used as {@code info.title} when the annotation leaves it blank
     * @return the model {@link Info} (never {@code null})
     */
    public static Info toInfo(final io.swagger.v3.oas.annotations.info.Info source,
                              final String fallbackTitle) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(fallbackTitle, "fallbackTitle");

        final Info info = new Info()
                .title(hasText(source.title()) ? source.title() : fallbackTitle)
                .version(hasText(source.version()) ? source.version() : DEFAULT_VERSION)
                .description(orNull(source.description()))
                .termsOfService(orNull(source.termsOfService()));

        toContact(source.contact()).ifPresent(info::contact);
        toLicense(source.license()).ifPresent(info::license);
        return info;
    }

    /**
     * @param sources the {@code @Server} annotations from {@code @ApiDomain}
     * @return the model server list (never {@code null}, possibly empty)
     */
    public static List<Server> toServers(final io.swagger.v3.oas.annotations.servers.Server[] sources) {
        return Arrays.stream(Objects.requireNonNull(sources, "sources"))
                .map(OpenApiMetadataMapper::toServer)
                .toList();
    }

    /**
     * @param sources the {@code @SecurityScheme} annotations from {@code @ApiDomain}
     * @return a map keyed by {@code @SecurityScheme.name()} for
     *         {@code components.securitySchemes}
     */
    public static Map<String, SecurityScheme> toSecuritySchemes(
            final io.swagger.v3.oas.annotations.security.SecurityScheme[] sources) {
        return Arrays.stream(Objects.requireNonNull(sources, "sources")).collect(Collectors.toMap(
                OpenApiMetadataMapper::securitySchemeKey,
                OpenApiMetadataMapper::toSecurityScheme,
                (first, second) -> {
                    throw new ApiContractConfigurationException(
                            "two @SecurityScheme entries share the same name");
                },
                LinkedHashMap::new));
    }

    private static Optional<Contact> toContact(
            final io.swagger.v3.oas.annotations.info.Contact source) {
        if (!hasText(source.name()) && !hasText(source.url()) && !hasText(source.email())) {
            return Optional.empty();
        }
        return Optional.of(new Contact()
                .name(orNull(source.name()))
                .url(orNull(source.url()))
                .email(orNull(source.email())));
    }

    private static Optional<License> toLicense(
            final io.swagger.v3.oas.annotations.info.License source) {
        if (!hasText(source.name()) && !hasText(source.url()) && !hasText(source.identifier())) {
            return Optional.empty();
        }
        final License license = new License().name(orNull(source.name())).url(orNull(source.url()));
        if (hasText(source.identifier())) {
            license.identifier(source.identifier());
        }
        return Optional.of(license);
    }

    private static Server toServer(final io.swagger.v3.oas.annotations.servers.Server source) {
        final Server server = new Server()
                .url(source.url())
                .description(orNull(source.description()));
        if (source.variables().length > 0) {
            server.variables(toServerVariables(source.variables()));
        }
        return server;
    }

    private static ServerVariables toServerVariables(
            final io.swagger.v3.oas.annotations.servers.ServerVariable[] sources) {
        return Arrays.stream(sources).collect(
                ServerVariables::new,
                (variables, source) -> variables.addServerVariable(source.name(), toServerVariable(source)),
                ServerVariables::putAll);
    }

    private static ServerVariable toServerVariable(
            final io.swagger.v3.oas.annotations.servers.ServerVariable source) {
        final ServerVariable variable = new ServerVariable()
                ._default(source.defaultValue())
                .description(orNull(source.description()));
        if (source.allowableValues().length > 0) {
            variable.setEnum(List.of(source.allowableValues()));
        }
        return variable;
    }

    private static String securitySchemeKey(
            final io.swagger.v3.oas.annotations.security.SecurityScheme source) {
        if (!hasText(source.name())) {
            throw new ApiContractConfigurationException(
                    "@SecurityScheme.name (the key under components.securitySchemes) must be set");
        }
        return source.name();
    }

    private static SecurityScheme toSecurityScheme(
            final io.swagger.v3.oas.annotations.security.SecurityScheme source) {
        if (DEFAULT_ENUM.equals(source.type().name())) {
            throw new ApiContractConfigurationException(
                    "@SecurityScheme(name=\"" + source.name() + "\") must declare a type");
        }
        final SecurityScheme scheme = new SecurityScheme()
                .type(SecurityScheme.Type.valueOf(source.type().name()))
                .description(orNull(source.description()))
                .name(orNull(source.paramName()))
                .$ref(orNull(source.ref()))
                .scheme(orNull(source.scheme()))
                .bearerFormat(orNull(source.bearerFormat()))
                .openIdConnectUrl(orNull(source.openIdConnectUrl()));
        if (!DEFAULT_ENUM.equals(source.in().name())) {
            scheme.setIn(SecurityScheme.In.valueOf(source.in().name()));
        }
        scheme.setFlows(toFlows(source.flows()));
        return scheme;
    }

    private static OAuthFlows toFlows(final io.swagger.v3.oas.annotations.security.OAuthFlows source) {
        final OAuthFlow implicit = toFlow(source.implicit());
        final OAuthFlow password = toFlow(source.password());
        final OAuthFlow clientCredentials = toFlow(source.clientCredentials());
        final OAuthFlow authorizationCode = toFlow(source.authorizationCode());

        if (Stream.of(implicit, password, clientCredentials, authorizationCode).allMatch(Objects::isNull)) {
            return null;
        }
        return new OAuthFlows()
                .implicit(implicit)
                .password(password)
                .clientCredentials(clientCredentials)
                .authorizationCode(authorizationCode);
    }

    private static OAuthFlow toFlow(final io.swagger.v3.oas.annotations.security.OAuthFlow source) {
        final boolean present = hasText(source.authorizationUrl()) || hasText(source.tokenUrl())
                || hasText(source.refreshUrl()) || source.scopes().length > 0;
        if (!present) {
            return null;
        }
        final OAuthFlow flow = new OAuthFlow()
                .authorizationUrl(orNull(source.authorizationUrl()))
                .tokenUrl(orNull(source.tokenUrl()))
                .refreshUrl(orNull(source.refreshUrl()));
        if (source.scopes().length > 0) {
            flow.setScopes(Arrays.stream(source.scopes()).collect(
                    Scopes::new,
                    (scopes, scope) -> scopes.addString(scope.name(), scope.description()),
                    Scopes::putAll));
        }
        return flow;
    }

    private static boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    private static String orNull(final String value) {
        return hasText(value) ? value : null;
    }
}
