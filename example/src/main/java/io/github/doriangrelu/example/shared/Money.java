package io.github.doriangrelu.example.shared;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A monetary amount in a given ISO-4217 currency. Part of the shared kernel: referenced by
 * both the {@code billing} and {@code catalog} domains, so it appears in both documents.
 *
 * @param amount   the amount, using the currency's minor-unit scale
 * @param currency the ISO-4217 currency code
 */
@Schema(name = "Money", description = "A monetary amount in a given currency")
public record Money(

        @Schema(example = "19.99", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

        @Schema(example = "EUR", requiredMode = Schema.RequiredMode.REQUIRED)
        String currency) {
}
