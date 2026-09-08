package io.openapigenerator.example.catalog.dto;

import io.openapigenerator.example.shared.Money;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A catalogue product.
 *
 * @param sku      stock-keeping unit, unique per product
 * @param name     display name
 * @param category product category
 * @param price    unit price
 * @param active   whether the product is currently sellable
 */
@Schema(name = "Product", description = "A catalogue product")
public record Product(

        @Schema(example = "SKU-000123")
        String sku,

        @Schema(example = "Wireless keyboard")
        String name,

        ProductCategory category,

        Money price,

        @Schema(example = "true")
        boolean active) {
}
