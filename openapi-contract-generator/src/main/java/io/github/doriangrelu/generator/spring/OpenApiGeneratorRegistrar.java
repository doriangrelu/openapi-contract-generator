package io.github.doriangrelu.generator.spring;

import java.util.List;

import io.github.doriangrelu.generator.domain.ApiDomainResolver;
import io.github.doriangrelu.generator.domain.ResolvedDomain;
import io.github.doriangrelu.generator.proxy.ContractProxyFactory;
import io.github.doriangrelu.generator.scan.ContractScanner;
import io.github.doriangrelu.generator.scan.ScannedContracts;
import org.springdoc.core.models.GroupedOpenApi;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * Discovers {@code @ApiContract} / {@code @ApiDomain} declarations and registers, per
 * bounded context:
 * <ul>
 *   <li>one documentation-only proxy bean per {@code @ApiContract} interface, so Spring
 *       MVC maps its routes;</li>
 *   <li>one {@link GroupedOpenApi} bean, so springdoc produces a separate document.</li>
 * </ul>
 *
 * <p>Imported by {@link OpenApiGeneratorApplication}. Runs during configuration parsing,
 * before springdoc auto-configuration consumes the {@code GroupedOpenApi} beans.
 *
 * <p>Internal API.
 */
final class OpenApiGeneratorRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private static final String PROXY_BEAN_PREFIX = "apiContract_";
    private static final String GROUP_BEAN_PREFIX = "groupedOpenApi_";

    private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

    @Override
    public void setBeanClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(final AnnotationMetadata importingClassMetadata,
                                        final BeanDefinitionRegistry registry) {
        final ScannedContracts scanned = new ContractScanner(classLoader).scan();
        final List<ResolvedDomain> domains = new ApiDomainResolver(classLoader).resolve(scanned);

        domains.forEach(domain -> {
            domain.contractInterfaces().forEach(contract -> registerProxy(registry, contract));
            registerGroup(registry, domain);
        });
    }

    private void registerProxy(final BeanDefinitionRegistry registry, final Class<?> contractInterface) {
        final Object proxy = ContractProxyFactory.create(contractInterface, classLoader);

        final RootBeanDefinition definition = new RootBeanDefinition();
        definition.setBeanClass(proxy.getClass());
        definition.setInstanceSupplier(() -> proxy);
        definition.setScope(BeanDefinition.SCOPE_SINGLETON);
        definition.setLazyInit(false);
        definition.setDescription("Documentation-only proxy for @ApiContract " + contractInterface.getName());

        registry.registerBeanDefinition(
                PROXY_BEAN_PREFIX + contractInterface.getName().replace('.', '_'), definition);
    }

    private static void registerGroup(final BeanDefinitionRegistry registry, final ResolvedDomain domain) {
        final RootBeanDefinition definition = new RootBeanDefinition(GroupedOpenApi.class);
        definition.setInstanceSupplier(() -> GroupedOpenApiFactory.forDomain(domain));
        definition.setDescription("OpenAPI group for @ApiDomain '" + domain.id() + "'");
        registry.registerBeanDefinition(GROUP_BEAN_PREFIX + domain.id(), definition);
    }
}
