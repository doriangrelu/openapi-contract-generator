package io.github.doriangrelu.example.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Top-level product category. */
@Schema(description = "Top-level product category")
public enum ProductCategory {
    BOOKS,
    ELECTRONICS,
    CLOTHING,
    GROCERY,
    OTHER
}
