package io.github.doriangrelu.generator.fixtures.valid.billing.deep;

import io.github.doriangrelu.contract.ApiContract;
import org.springframework.web.bind.annotation.GetMapping;

/** Fixture contract in a sub-package of {@code billing}; attributed to {@code billing}. */
@ApiContract("/billing/deep")
public interface DeepBillingApi {

    @GetMapping
    String list();
}
