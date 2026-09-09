package io.github.doriangrelu.example.billing.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import io.github.doriangrelu.example.shared.Money;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * An invoice as returned by the API.
 *
 * @param id         opaque invoice identifier
 * @param customerId identifier of the billed customer
 * @param status     current lifecycle status
 * @param lines      billable lines
 * @param total      sum of all lines
 * @param dueDate    date payment is due
 * @param issuedAt   instant the invoice was issued, {@code null} while {@code DRAFT}
 */
@Schema(name = "Invoice", description = "An invoice")
public record Invoice(

        @Schema(example = "inv_42")
        String id,

        @Schema(example = "cus_1001")
        String customerId,

        InvoiceStatus status,

        List<InvoiceLine> lines,

        Money total,

        @Schema(example = "2026-10-15")
        LocalDate dueDate,

        @Schema(example = "2026-09-15T09:30:00Z", nullable = true)
        Instant issuedAt) {
}
