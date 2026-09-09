package io.github.doriangrelu.generator.fixtures.valid.catalog;

import io.github.doriangrelu.contract.ApiContract;
import org.springframework.web.bind.annotation.GetMapping;

/** Fixture contract in the {@code catalog} domain package. */
@ApiContract("/catalog")
public interface CatalogApi {

    @GetMapping
    String list();
}
