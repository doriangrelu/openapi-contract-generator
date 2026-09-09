package io.github.doriangrelu.generator.fixtures.valid.billing;

import io.github.doriangrelu.contract.ApiContract;
import org.springframework.web.bind.annotation.GetMapping;

/** Fixture contract in the {@code billing} domain package. */
@ApiContract("/billing")
public interface BillingApi {

    @GetMapping("/ping")
    String ping();
}
