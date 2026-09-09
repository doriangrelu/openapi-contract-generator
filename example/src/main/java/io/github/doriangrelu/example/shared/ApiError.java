package io.github.doriangrelu.example.shared;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Problem details payload (RFC&nbsp;9457, {@code application/problem+json}). Shared kernel
 * type used by error responses in every domain.
 *
 * @param type     a URI reference identifying the problem type
 * @param title    a short, human-readable summary of the problem type
 * @param status   the HTTP status code
 * @param detail   a human-readable explanation specific to this occurrence
 * @param instance a URI reference identifying this specific occurrence
 */
@Schema(name = "ApiError", description = "RFC 9457 problem details")
public record ApiError(

        @Schema(example = "https://errors.example.com/not-found")
        String type,

        @Schema(example = "Not Found")
        String title,

        @Schema(example = "404")
        int status,

        @Schema(example = "No invoice with id inv_42")
        String detail,

        @Schema(example = "/invoices/inv_42")
        String instance) {
}
