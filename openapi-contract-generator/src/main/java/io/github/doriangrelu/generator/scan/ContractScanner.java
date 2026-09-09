package io.github.doriangrelu.generator.scan;

import java.util.List;
import java.util.Objects;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.PackageInfo;
import io.github.classgraph.ScanResult;
import io.github.doriangrelu.contract.ApiContract;
import io.github.doriangrelu.contract.ApiDomain;

/**
 * Whole-classpath scan for {@link ApiContract @ApiContract} interfaces and
 * {@link ApiDomain @ApiDomain} packages, backed by ClassGraph.
 *
 * <p>No base package needs to be configured: the marker annotations are unique to this
 * library, so the scan is unambiguous. A short reject list trims the obvious
 * infrastructure namespaces to keep it fast.
 *
 * <p>Internal API.
 */
public final class ContractScanner {

    private static final String[] REJECTED_PACKAGES = {
            "org.springframework", "org.springdoc", "io.swagger", "io.github.classgraph",
            "com.fasterxml.jackson", "org.junit", "org.opentest4j", "org.apiguardian",
            "org.assertj", "org.mockito", "org.slf4j", "ch.qos.logback", "org.yaml",
            "org.apache.catalina", "org.apache.coyote", "org.apache.tomcat", "org.apache.juli"
    };

    private final ClassLoader classLoader;

    /**
     * @param classLoader the class loader whose classpath is scanned
     */
    public ContractScanner(final ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /**
     * Runs the scan.
     *
     * @return the discovered {@code @ApiContract} interfaces and {@code @ApiDomain} package
     *         names
     */
    public ScannedContracts scan() {
        try (ScanResult result = newClassGraph().scan()) {
            final List<Class<?>> contractInterfaces = result
                    .getClassesWithAnnotation(ApiContract.class)
                    .stream()
                    .filter(ClassInfo::isInterface)
                    .<Class<?>>map(ClassInfo::loadClass)
                    .toList();

            final List<String> domainPackages = result.getPackageInfo().stream()
                    .filter(pkg -> pkg.hasAnnotation(ApiDomain.class.getName()))
                    .map(PackageInfo::getName)
                    .toList();

            return new ScannedContracts(contractInterfaces, domainPackages);
        }
    }

    private ClassGraph newClassGraph() {
        return new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .overrideClassLoaders(classLoader)
                .rejectPackages(REJECTED_PACKAGES);
    }
}
