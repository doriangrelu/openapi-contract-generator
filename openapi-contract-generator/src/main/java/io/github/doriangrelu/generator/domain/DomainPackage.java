package io.github.doriangrelu.generator.domain;

import java.util.Objects;

import io.github.doriangrelu.contract.ApiDomain;

/**
 * A package that carries {@link ApiDomain @ApiDomain}, paired with the annotation instance
 * read from its {@code package-info}.
 *
 * @param packageName fully-qualified package name
 * @param annotation  the {@code @ApiDomain} declared on that package
 */
public record DomainPackage(String packageName, ApiDomain annotation) {

    public DomainPackage {
        Objects.requireNonNull(packageName, "packageName");
        Objects.requireNonNull(annotation, "annotation");
    }

    /** @return the domain id ({@link ApiDomain#id()}) */
    public String id() {
        return annotation.id();
    }

    /**
     * @param other another package name
     * @return {@code true} if {@code other} is this package or a sub-package of it
     */
    boolean contains(final String other) {
        return other.equals(packageName) || other.startsWith(packageName + ".");
    }
}
