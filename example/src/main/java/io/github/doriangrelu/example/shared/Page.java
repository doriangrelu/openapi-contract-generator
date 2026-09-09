package io.github.doriangrelu.example.shared;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A generic page of results. Exercises generic type resolution: used as
 * {@code Page<Invoice>} / {@code Page<Product>}, swagger-core emits {@code PageInvoice} and
 * {@code PageProduct} schemas.
 *
 * @param <T>           the element type
 * @param content       elements on the current page
 * @param page          zero-based page index
 * @param size          requested page size
 * @param totalElements total number of elements across all pages
 */
@Schema(description = "A page of results")
public record Page<T>(

        List<T> content,

        @Schema(example = "0")
        int page,

        @Schema(example = "20")
        int size,

        @Schema(example = "137")
        long totalElements) {
}
