package io.github.doriangrelu.generator.fixtures.orphan;

import io.github.doriangrelu.contract.ApiContract;
import org.springframework.web.bind.annotation.GetMapping;

/** Fixture contract with no {@code @ApiDomain} ancestor package. */
@ApiContract("/orphan")
public interface OrphanApi {

    @GetMapping
    String list();
}
