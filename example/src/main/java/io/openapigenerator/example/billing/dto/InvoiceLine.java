package io.openapigenerator.example.billing.dto;

import io.openapigenerator.example.shared.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A single billable line on an invoice.
 *
 * @param description free-text description of the item
 * @param quantity    number of units (at least 1)
 * @param unitPrice   price per unit
 */
@Schema(name = "InvoiceLine", description = "A single billable line on an invoice")
public record InvoiceLine(

        @Schema(example = "Annual subscription", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String description,

        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(1)
        int quantity,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Valid
        Money unitPrice) {
}
