package io.openapigenerator.example.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Lifecycle status of an invoice. */
@Schema(description = "Lifecycle status of an invoice")
public enum InvoiceStatus {

    /** Editable, not yet sent to the customer. */
    DRAFT,

    /** Sent to the customer, awaiting payment. */
    ISSUED,

    /** Fully paid. */
    PAID,

    /** Past due date and unpaid. */
    OVERDUE,

    /** Voided; no longer collectible. */
    CANCELLED
}
