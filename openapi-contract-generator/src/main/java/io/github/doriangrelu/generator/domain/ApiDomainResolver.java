package io.github.doriangrelu.generator.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.doriangrelu.contract.ApiDomain;
import io.github.doriangrelu.generator.ApiContractConfigurationException;
import io.github.doriangrelu.generator.scan.ScannedContracts;

/**
 * Turns raw scan output into a validated list of {@link ResolvedDomain}s, or fails with
 * {@link ApiContractConfigurationException}.
 *
 * <p>Rules enforced:
 * <ul>
 *   <li>{@link ApiDomain#id()} matches {@code ^[a-z0-9][a-z0-9-]*$};</li>
 *   <li>ids are unique across domain packages;</li>
 *   <li>no {@code @ApiDomain} package is nested inside another;</li>
 *   <li>every {@code @ApiContract} interface has exactly one {@code @ApiDomain} ancestor
 *       (its own package or a parent).</li>
 * </ul>
 *
 * <p>Internal API.
 */
public final class ApiDomainResolver {

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]*$");

    private final ClassLoader classLoader;

    /**
     * @param classLoader class loader used to load {@code package-info} types
     */
    public ApiDomainResolver(final ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /**
     * @param scanned the {@code @ApiContract} interfaces and {@code @ApiDomain} package names
     * @return one {@link ResolvedDomain} per declared domain, in declaration order
     * @throws ApiContractConfigurationException if any rule is violated
     */
    public List<ResolvedDomain> resolve(final ScannedContracts scanned) {
        Objects.requireNonNull(scanned, "scanned");

        final List<DomainPackage> domains = loadDomains(scanned.domainPackages());
        validateIdSyntax(domains);
        validateIdUniqueness(domains);
        validateNoNesting(domains);

        final Map<DomainPackage, List<Class<?>>> membership = scanned.contractInterfaces().stream()
                .collect(Collectors.groupingBy(contract -> nearestDomain(contract, domains)
                        .orElseThrow(() -> orphan(contract, domains))));

        return domains.stream()
                .map(domain -> new ResolvedDomain(domain.id(), domain.annotation(), domain.packageName(),
                        Set.copyOf(membership.getOrDefault(domain, List.of()))))
                .toList();
    }

    private List<DomainPackage> loadDomains(final List<String> packageNames) {
        return packageNames.stream()
                .map(name -> new DomainPackage(name, readApiDomain(name)))
                .toList();
    }

    /**
     * Loads the {@link ApiDomain} instance from the synthetic {@code package-info} type,
     * whose binary name is {@code <package>.package-info} — not a legal source identifier
     * but a valid argument to {@link Class#forName(String, boolean, ClassLoader)}.
     */
    private ApiDomain readApiDomain(final String packageName) {
        final String binaryName = packageName + ".package-info";
        try {
            final ApiDomain annotation = Class.forName(binaryName, false, classLoader)
                    .getAnnotation(ApiDomain.class);
            if (annotation == null) {
                throw new ApiContractConfigurationException(
                        "Package " + packageName + " was reported as @ApiDomain but the annotation "
                        + "could not be read from " + binaryName);
            }
            return annotation;
        }
        catch (final ClassNotFoundException ex) {
            throw new ApiContractConfigurationException(
                    "Cannot load " + binaryName + " to read @ApiDomain", ex);
        }
    }

    private static void validateIdSyntax(final List<DomainPackage> domains) {
        domains.stream()
                .filter(domain -> !ID_PATTERN.matcher(domain.id()).matches())
                .findFirst()
                .ifPresent(domain -> {
                    throw new ApiContractConfigurationException(
                            "@ApiDomain id '" + domain.id() + "' on package " + domain.packageName()
                            + " must match " + ID_PATTERN.pattern());
                });
    }

    private static void validateIdUniqueness(final List<DomainPackage> domains) {
        domains.stream()
                .collect(Collectors.groupingBy(DomainPackage::id,
                        Collectors.mapping(DomainPackage::packageName, Collectors.toList())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .ifPresent(entry -> {
                    throw new ApiContractConfigurationException(
                            "@ApiDomain id '" + entry.getKey() + "' is declared by several packages: "
                            + entry.getValue());
                });
    }

    private static void validateNoNesting(final List<DomainPackage> domains) {
        domains.stream()
                .flatMap(outer -> domains.stream()
                        .filter(inner -> inner != outer && outer.contains(inner.packageName()))
                        .map(inner -> Map.entry(outer, inner)))
                .findFirst()
                .ifPresent(pair -> {
                    throw new ApiContractConfigurationException(
                            "@ApiDomain package " + pair.getValue().packageName() + " is nested inside "
                            + "@ApiDomain package " + pair.getKey().packageName()
                            + "; a domain package may not contain another domain package.");
                });
    }

    private static Optional<DomainPackage> nearestDomain(final Class<?> contract,
                                                        final List<DomainPackage> domains) {
        final String contractPackage = contract.getPackageName();
        return domains.stream()
                .filter(domain -> domain.contains(contractPackage))
                .max(Comparator.comparingInt(domain -> domain.packageName().length()));
    }

    private static ApiContractConfigurationException orphan(final Class<?> contract,
                                                           final List<DomainPackage> domains) {
        final List<String> known = domains.stream().map(DomainPackage::packageName).toList();
        return new ApiContractConfigurationException(
                "@ApiContract interface " + contract.getName() + " is not inside any @ApiDomain "
                + "package. Known domain packages: " + known + ". Add @ApiDomain to a "
                + "package-info.java on its package or an ancestor.");
    }
}
