package io.openapigenerator.example.billing.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload to create a new invoice in {@code DRAFT} status.
 *
 * @param customerId identifier of the customer to bill
 * @param lines      at least one billable line
 * @param dueDate    a future due date
 */
@Schema(name = "CreateInvoiceRequest", description = "Payload to create a draft invoice")
public record CreateInvoiceRequest(

        @Schema(example = "cus_1001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String customerId,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Size(max = 200) @Valid
        List<InvoiceLine> lines,

        @Schema(example = "2026-10-15", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Future
        LocalDate dueDate) {
}
