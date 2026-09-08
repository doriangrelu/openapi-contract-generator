package io.openapigenerator.generator;

import java.util.Set;

import io.openapigenerator.contract.ApiDomain;

/**
 * A validated bounded context: one {@link ApiDomain @ApiDomain} package together with the
 * {@code @ApiContract} interfaces attributed to it by package containment.
 *
 * @param id                 the domain id ({@link ApiDomain#id()}); also the springdoc
 *                           group name and the generated file base name
 * @param declaration        the annotation instance read from the {@code package-info}
 * @param packageName        the package carrying {@code @ApiDomain}
 * @param contractInterfaces the interfaces contributing operations to this domain
 *                           (defensively copied, never empty)
 */
public record ResolvedDomain(
        String id,
        ApiDomain declaration,
        String packageName,
        Set<Class<?>> contractInterfaces) {

    /**
     * Canonical constructor; copies {@code contractInterfaces} into an immutable set.
     */
    public ResolvedDomain {
        contractInterfaces = Set.copyOf(contractInterfaces);
    }
}
