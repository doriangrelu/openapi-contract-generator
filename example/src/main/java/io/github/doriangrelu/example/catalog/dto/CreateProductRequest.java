package io.github.doriangrelu.example.catalog.dto;

import io.github.doriangrelu.example.shared.Money;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload to add a product to the catalogue.
 *
 * @param sku      stock-keeping unit ({@code SKU-} followed by 6 digits)
 * @param name     display name
 * @param category product category
 * @param price    unit price
 */
@Schema(name = "CreateProductRequest", description = "Payload to add a product")
public record CreateProductRequest(

        @Schema(example = "SKU-000123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Pattern(regexp = "SKU-\\d{6}")
        String sku,

        @Schema(example = "Wireless keyboard", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 120)
        String name,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        ProductCategory category,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Valid
        Money price) {
}
