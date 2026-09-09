package io.github.doriangrelu.generator.scan;

import java.util.List;
import java.util.Objects;

/**
 * Immutable result of a {@link ContractScanner} run.
 *
 * @param contractInterfaces interfaces bearing {@code @ApiContract}, in classpath order
 * @param domainPackages      names of packages bearing {@code @ApiDomain}, in classpath order
 */
public record ScannedContracts(List<Class<?>> contractInterfaces, List<String> domainPackages) {

    /**
     * Canonical constructor; defensively copies both lists into immutable ones.
     */
    public ScannedContracts {
        contractInterfaces = List.copyOf(Objects.requireNonNull(contractInterfaces, "contractInterfaces"));
        domainPackages = List.copyOf(Objects.requireNonNull(domainPackages, "domainPackages"));
    }
}
