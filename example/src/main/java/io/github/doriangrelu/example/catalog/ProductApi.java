package io.github.doriangrelu.example.catalog;

import io.github.doriangrelu.contract.ApiContract;
import io.github.doriangrelu.example.catalog.dto.CreateProductRequest;
import io.github.doriangrelu.example.catalog.dto.Product;
import io.github.doriangrelu.example.catalog.dto.ProductCategory;
import io.github.doriangrelu.example.shared.ApiError;
import io.github.doriangrelu.example.shared.Page;
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
 * Product endpoints of the catalog domain. Shares {@link Page}, {@link ApiError} and
 * {@code Money} with the billing domain; each shared type is resolved independently into
 * {@code catalog.yaml}.
 */
@ApiContract("/products")
@Tag(name = "Products", description = "Browse and manage catalogue products")
public interface ProductApi {

    /**
     * Lists products, optionally filtered by category.
     *
     * @param category optional category filter
     * @param page     zero-based page index
     * @param size     page size, 1..100
     * @return a page of products
     */
    @Operation(summary = "List products")
    @GetMapping
    Page<Product> listProducts(
            @Parameter(description = "Only return products in this category")
            @RequestParam(name = "category", required = false) ProductCategory category,
            @Parameter(description = "Zero-based page index")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size (1..100)")
            @RequestParam(name = "size", defaultValue = "20") int size);

    /**
     * Returns a single product by SKU.
     *
     * @param sku the stock-keeping unit
     * @return the product
     */
    @Operation(summary = "Get a product by SKU")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "No product with that SKU",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{sku}")
    Product getProduct(
            @Parameter(description = "Stock-keeping unit", example = "SKU-000123")
            @PathVariable("sku") String sku);

    /**
     * Adds a product to the catalogue.
     *
     * @param request the product to add
     * @return the created product
     */
    @Operation(summary = "Add a product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Invalid payload",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    Product createProduct(@RequestBody @Valid CreateProductRequest request);
}
