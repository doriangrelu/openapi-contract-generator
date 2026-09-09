package io.github.doriangrelu.generator.spring;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import io.github.doriangrelu.contract.ApiDomain;
import io.github.doriangrelu.generator.domain.ResolvedDomain;
import io.github.doriangrelu.generator.openapi.OpenApiMetadataMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;

/**
 * Builds one springdoc {@link GroupedOpenApi} per {@link ResolvedDomain}: a method filter
 * that keeps only the domain's contract interfaces, plus a customizer that injects the
 * {@code info} / {@code servers} / {@code securitySchemes} declared on {@code @ApiDomain}.
 *
 * <p>Internal API.
 */
final class GroupedOpenApiFactory {

    private static final String ALL_PATHS = "/**";

    private GroupedOpenApiFactory() {
    }

    static GroupedOpenApi forDomain(final ResolvedDomain domain) {
        final Set<Class<?>> members = domain.contractInterfaces();
        final ApiDomain declaration = domain.declaration();
        return GroupedOpenApi.builder()
                .group(domain.id())
                .pathsToMatch(ALL_PATHS)
                .addOpenApiMethodFilter(method -> belongsTo(method, members))
                .addOpenApiCustomizer(openApi -> applyMetadata(openApi, declaration, domain.id()))
                .build();
    }

    private static void applyMetadata(final OpenAPI openApi, final ApiDomain declaration,
                                      final String domainId) {
        openApi.setInfo(OpenApiMetadataMapper.toInfo(declaration.info(), domainId));
        if (declaration.servers().length > 0) {
            openApi.setServers(OpenApiMetadataMapper.toServers(declaration.servers()));
        }
        if (declaration.securitySchemes().length > 0) {
            final Map<String, SecurityScheme> schemes =
                    OpenApiMetadataMapper.toSecuritySchemes(declaration.securitySchemes());
            final Components components = openApi.getComponents() != null
                    ? openApi.getComponents() : new Components();
            schemes.forEach(components::addSecuritySchemes);
            openApi.setComponents(components);
        }
    }

    /**
     * A method belongs to the domain when its declaring type is one of the domain's
     * contract interfaces, or — for the JDK proxy case, where the declaring type is the
     * synthetic {@code $ProxyN} class — when one of that type's interfaces is.
     *
     * @param method  a handler method reported by Spring MVC
     * @param members the domain's contract interfaces
     * @return whether the method belongs to the domain
     */
    static boolean belongsTo(final Method method, final Set<Class<?>> members) {
        final Class<?> declaringClass = method.getDeclaringClass();
        return members.contains(declaringClass)
                || Arrays.stream(declaringClass.getInterfaces()).anyMatch(members::contains);
    }
}
