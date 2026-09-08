package io.openapigenerator.example.billing;

import io.openapigenerator.contract.ApiContract;
import io.openapigenerator.example.billing.dto.CreateInvoiceRequest;
import io.openapigenerator.example.billing.dto.Invoice;
import io.openapigenerator.example.billing.dto.InvoiceStatus;
import io.openapigenerator.example.shared.ApiError;
import io.openapigenerator.example.shared.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Invoice endpoints of the billing domain. No implementation lives in this module: the
 * generator registers a documentation-only proxy and springdoc turns this contract into
 * {@code billing.yaml}.
 */
@ApiContract("/invoices")
@Tag(name = "Invoices", description = "Create and query invoices")
public interface InvoiceApi {

    /**
     * Lists invoices, most recent first, optionally filtered by status.
     *
     * @param status optional status filter
     * @param page   zero-based page index
     * @param size   page size, 1..100
     * @return a page of invoices
     */
    @Operation(summary = "List invoices")
    @GetMapping
    Page<Invoice> listInvoices(
            @Parameter(description = "Only return invoices in this status")
            @RequestParam(name = "status", required = false) InvoiceStatus status,
            @Parameter(description = "Zero-based page index")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size (1..100)")
            @RequestParam(name = "size", defaultValue = "20") int size);

    /**
     * Returns a single invoice by id.
     *
     * @param invoiceId the invoice id
     * @return the invoice
     */
    @Operation(summary = "Get an invoice by id")
    @ApiResponse(responseCode = "200", description = "Invoice found")
    @ApiResponse(responseCode = "404", description = "No invoice with that id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{invoiceId}")
    Invoice getInvoice(
            @Parameter(description = "Invoice id", example = "inv_42")
            @PathVariable("invoiceId") String invoiceId);

    /**
     * Creates a new invoice in {@code DRAFT} status.
     *
     * @param request the invoice to create
     * @return the created invoice
     */
    @Operation(summary = "Create a draft invoice")
    @ApiResponse(responseCode = "201", description = "Invoice created")
    @ApiResponse(responseCode = "400", description = "Invalid payload",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    Invoice createInvoice(@RequestBody @Valid CreateInvoiceRequest request);
}
