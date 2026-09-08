package io.openapigenerator.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import io.openapigenerator.contract.ApiDomain;

/**
 * Turns a raw {@link ContractScanner.Result} into a validated list of
 * {@link ResolvedDomain}s, or fails with {@link ApiContractConfigurationException}.
 *
 * <p>Rules enforced:
 * <ul>
 *   <li>{@link ApiDomain#id()} matches {@code ^[a-z0-9][a-z0-9-]*$};</li>
 *   <li>ids are unique across domain packages;</li>
 *   <li>no {@code @ApiDomain} package is nested inside another;</li>
 *   <li>every {@code @ApiContract} interface has exactly one {@code @ApiDomain} ancestor
 *       (its own package or a parent).</li>
 * </ul>
 */
final class ApiDomainResolver {

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]*$");

    private final ClassLoader classLoader;

    ApiDomainResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    List<ResolvedDomain> resolve(ContractScanner.Result scan) {
        List<DomainPackage> domains = loadDomains(scan.domainPackages());
        validateIds(domains);
        validateNoNesting(domains);

        Map<DomainPackage, Set<Class<?>>> membership = new LinkedHashMap<>();
        domains.forEach(domain -> membership.put(domain, new LinkedHashSet<>()));

        for (Class<?> contract : scan.contractInterfaces()) {
            DomainPackage owner = nearestDomain(contract, domains);
            if (owner == null) {
                throw new ApiContractConfigurationException(
                        "@ApiContract interface " + contract.getName() + " is not inside any "
                        + "@ApiDomain package. Known domain packages: " + packageNames(domains)
                        + ". Add @ApiDomain to a package-info.java on its package or an ancestor.");
            }
            membership.get(owner).add(contract);
        }

        List<ResolvedDomain> resolved = new ArrayList<>();
        membership.forEach((domain, interfaces) -> resolved.add(new ResolvedDomain(
                domain.annotation().id(), domain.annotation(), domain.packageName(), interfaces)));
        return List.copyOf(resolved);
    }

    private List<DomainPackage> loadDomains(List<String> packageNames) {
        List<DomainPackage> domains = new ArrayList<>();
        for (String packageName : packageNames) {
            domains.add(new DomainPackage(packageName, readApiDomain(packageName)));
        }
        return domains;
    }

    /**
     * Loads the {@link ApiDomain} instance from the synthetic {@code package-info} type.
     * Its binary name is {@code <package>.package-info}: not a legal source identifier,
     * but a valid argument to {@link Class#forName(String, boolean, ClassLoader)}.
     */
    private ApiDomain readApiDomain(String packageName) {
        String binaryName = packageName + ".package-info";
        try {
            Class<?> packageInfo = Class.forName(binaryName, false, classLoader);
            ApiDomain annotation = packageInfo.getAnnotation(ApiDomain.class);
            if (annotation == null) {
                throw new ApiContractConfigurationException(
                        "Package " + packageName + " was reported as @ApiDomain but the annotation "
                        + "could not be read from " + binaryName);
            }
            return annotation;
        }
        catch (ClassNotFoundException ex) {
            throw new ApiContractConfigurationException(
                    "Cannot load " + binaryName + " to read @ApiDomain", ex);
        }
    }

    private void validateIds(List<DomainPackage> domains) {
        Map<String, List<String>> packagesById = new LinkedHashMap<>();
        for (DomainPackage domain : domains) {
            String id = domain.annotation().id();
            if (!ID_PATTERN.matcher(id).matches()) {
                throw new ApiContractConfigurationException(
                        "@ApiDomain id '" + id + "' on package " + domain.packageName()
                        + " must match " + ID_PATTERN.pattern());
            }
            packagesById.computeIfAbsent(id, key -> new ArrayList<>()).add(domain.packageName());
        }
        packagesById.forEach((id, packages) -> {
            if (packages.size() > 1) {
                throw new ApiContractConfigurationException(
                        "@ApiDomain id '" + id + "' is declared by several packages: " + packages);
            }
        });
    }

    private void validateNoNesting(List<DomainPackage> domains) {
        for (DomainPackage outer : domains) {
            for (DomainPackage inner : domains) {
                if (outer != inner && isUnder(inner.packageName(), outer.packageName())) {
                    throw new ApiContractConfigurationException(
                            "@ApiDomain package " + inner.packageName() + " is nested inside "
                            + "@ApiDomain package " + outer.packageName()
                            + "; a domain package may not contain another domain package.");
                }
            }
        }
    }

    private DomainPackage nearestDomain(Class<?> contract, List<DomainPackage> domains) {
        String contractPackage = contract.getPackageName();
        DomainPackage nearest = null;
        for (DomainPackage domain : domains) {
            boolean contained = contractPackage.equals(domain.packageName())
                    || isUnder(contractPackage, domain.packageName());
            if (contained && (nearest == null
                    || domain.packageName().length() > nearest.packageName().length())) {
                nearest = domain;
            }
        }
        return nearest;
    }

    private static boolean isUnder(String candidate, String ancestor) {
        return candidate.startsWith(ancestor + ".");
    }

    private static List<String> packageNames(List<DomainPackage> domains) {
        return domains.stream().map(DomainPackage::packageName).toList();
    }

    private record DomainPackage(String packageName, ApiDomain annotation) {
    }
}
