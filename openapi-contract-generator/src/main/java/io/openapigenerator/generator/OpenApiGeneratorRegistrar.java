package io.openapigenerator.generator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openapigenerator.contract.ApiDomain;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * Discovers {@code @ApiContract} / {@link ApiDomain @ApiDomain} declarations and registers,
 * per bounded context:
 * <ul>
 *   <li>one documentation-only proxy bean per {@code @ApiContract} interface, so Spring
 *       MVC maps its routes;</li>
 *   <li>one {@link GroupedOpenApi} bean, so springdoc produces a separate document whose
 *       {@code info} / {@code servers} / {@code securitySchemes} come from
 *       {@code @ApiDomain}.</li>
 * </ul>
 *
 * <p>Imported by {@link OpenApiGeneratorApplication}. Runs during configuration parsing,
 * before springdoc auto-configuration consumes the {@code GroupedOpenApi} beans.
 */
final class OpenApiGeneratorRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {

        ContractScanner.Result scan = new ContractScanner(classLoader).scan();
        List<ResolvedDomain> domains = new ApiDomainResolver(classLoader).resolve(scan);

        for (ResolvedDomain domain : domains) {
            domain.contractInterfaces().forEach(contract -> registerProxy(registry, contract));
            registerGroup(registry, domain);
        }
    }

    private void registerProxy(BeanDefinitionRegistry registry, Class<?> contractInterface) {
        Object proxy = ContractProxyFactory.create(contractInterface, classLoader);

        RootBeanDefinition definition = new RootBeanDefinition();
        definition.setBeanClass(proxy.getClass());
        definition.setInstanceSupplier(() -> proxy);
        definition.setScope(BeanDefinition.SCOPE_SINGLETON);
        definition.setLazyInit(false);
        definition.setDescription("Documentation-only proxy for @ApiContract " + contractInterface.getName());

        registry.registerBeanDefinition(proxyBeanName(contractInterface), definition);
    }

    private void registerGroup(BeanDefinitionRegistry registry, ResolvedDomain domain) {
        RootBeanDefinition definition = new RootBeanDefinition(GroupedOpenApi.class);
        definition.setInstanceSupplier(() -> buildGroup(domain));
        definition.setDescription("OpenAPI group for @ApiDomain '" + domain.id() + "'");
        registry.registerBeanDefinition("groupedOpenApi_" + domain.id(), definition);
    }

    private GroupedOpenApi buildGroup(ResolvedDomain domain) {
        Set<Class<?>> members = domain.contractInterfaces();
        ApiDomain declaration = domain.declaration();

        return GroupedOpenApi.builder()
                .group(domain.id())
                .pathsToMatch("/**")
                .addOpenApiMethodFilter(method -> belongsTo(method, members))
                .addOpenApiCustomizer(openApi -> {
                    openApi.setInfo(SwaggerAnnotationTranslator.toInfo(declaration.info(), domain.id()));
                    if (declaration.servers().length > 0) {
                        openApi.setServers(SwaggerAnnotationTranslator.toServers(declaration.servers()));
                    }
                    if (declaration.securitySchemes().length > 0) {
                        applySecuritySchemes(openApi.getComponents(), declaration, openApi);
                    }
                })
                .build();
    }

    private static void applySecuritySchemes(Components existing, ApiDomain declaration,
                                             io.swagger.v3.oas.models.OpenAPI openApi) {
        Map<String, SecurityScheme> schemes =
                SwaggerAnnotationTranslator.toSecuritySchemes(declaration.securitySchemes());
        Components components = existing != null ? existing : new Components();
        schemes.forEach(components::addSecuritySchemes);
        openApi.setComponents(components);
    }

    /**
     * A method belongs to the domain when its declaring type is one of the domain's
     * contract interfaces, or — for the JDK proxy case, where the declaring type is the
     * synthetic {@code $ProxyN} class — when one of that type's interfaces is.
     */
    private static boolean belongsTo(Method method, Set<Class<?>> members) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (members.contains(declaringClass)) {
            return true;
        }
        for (Class<?> implemented : declaringClass.getInterfaces()) {
            if (members.contains(implemented)) {
                return true;
            }
        }
        return false;
    }

    private static String proxyBeanName(Class<?> contractInterface) {
        return "apiContract_" + contractInterface.getName().replace('.', '_');
    }
}
