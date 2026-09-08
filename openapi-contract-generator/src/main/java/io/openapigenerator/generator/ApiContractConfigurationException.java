package io.openapigenerator.generator;

import java.io.Serial;

/**
 * Thrown during context initialisation when the {@code @ApiContract} / {@code @ApiDomain}
 * declarations found on the classpath are inconsistent: an orphan contract interface,
 * nested domain packages, or a duplicate / malformed domain id.
 *
 * <p>The failure surfaces as a context-startup error on the {@code OpenApiSpecGeneratorTest}
 * subclass, with this exception as the cause.
 */
public class ApiContractConfigurationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param message human-readable description of the inconsistency
     */
    public ApiContractConfigurationException(String message) {
        super(message);
    }

    /**
     * @param message human-readable description of the inconsistency
     * @param cause   the underlying failure
     */
    public ApiContractConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
