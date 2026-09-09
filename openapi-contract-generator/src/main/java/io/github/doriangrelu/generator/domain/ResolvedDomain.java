package io.github.doriangrelu.generator.domain;

import java.util.Objects;
import java.util.Set;

import io.github.doriangrelu.contract.ApiDomain;

/**
 * A validated bounded context: one {@link ApiDomain @ApiDomain} package together with the
 * {@code @ApiContract} interfaces attributed to it by package containment.
 *
 * @param id                 the domain id ({@link ApiDomain#id()}); also the springdoc
 *                           group name and the generated file base name
 * @param declaration        the annotation instance read from the {@code package-info}
 * @param packageName        the package carrying {@code @ApiDomain}
 * @param contractInterfaces the interfaces contributing operations to this domain
 *                           (immutable copy, possibly empty)
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
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(declaration, "declaration");
        Objects.requireNonNull(packageName, "packageName");
        contractInterfaces = Set.copyOf(Objects.requireNonNull(contractInterfaces, "contractInterfaces"));
    }
}
