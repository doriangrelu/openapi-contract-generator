package io.github.doriangrelu.generator;

import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.PackageInfo;
import io.github.classgraph.ScanResult;
import io.github.doriangrelu.contract.ApiContract;
import io.github.doriangrelu.contract.ApiDomain;

/**
 * Classpath scan for {@link ApiContract @ApiContract} interfaces and
 * {@link ApiDomain @ApiDomain} packages, backed by ClassGraph.
 *
 * <p>No base package needs to be configured: the marker annotations are unique to this
 * library, so a whole-classpath scan is unambiguous. A short reject list trims the
 * obvious infrastructure namespaces to keep the scan fast.
 */
final class ContractScanner {

    private static final String[] REJECTED_PACKAGES = {
            "org.springframework", "org.springdoc", "io.swagger", "io.github.classgraph",
            "com.fasterxml.jackson", "org.junit", "org.opentest4j", "org.apiguardian",
            "org.assertj", "org.mockito", "org.slf4j", "ch.qos.logback", "org.yaml",
            "org.apache.catalina", "org.apache.coyote", "org.apache.tomcat", "org.apache.juli"
    };

    private final ClassLoader classLoader;

    ContractScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Runs the scan.
     *
     * @return the {@code @ApiContract} interfaces and the names of the {@code @ApiDomain}
     *         packages present on the classpath
     */
    Result scan() {
        try (ScanResult result = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .overrideClassLoaders(classLoader)
                .rejectPackages(REJECTED_PACKAGES)
                .scan()) {

            List<Class<?>> contractInterfaces = result
                    .getClassesWithAnnotation(ApiContract.class)
                    .stream()
                    .filter(ClassInfo::isInterface)
                    .map(ClassInfo::loadClass)
                    .toList();

            List<String> domainPackages = result.getPackageInfo().stream()
                    .filter(pkg -> pkg.hasAnnotation(ApiDomain.class.getName()))
                    .map(PackageInfo::getName)
                    .toList();

            return new Result(contractInterfaces, domainPackages);
        }
    }

    /**
     * @param contractInterfaces interfaces bearing {@code @ApiContract}
     * @param domainPackages      names of packages bearing {@code @ApiDomain}
     */
    record Result(List<Class<?>> contractInterfaces, List<String> domainPackages) {
    }
}
