package io.github.doriangrelu.generator.domain;

import java.util.List;

import io.github.doriangrelu.generator.ApiContractConfigurationException;
import io.github.doriangrelu.generator.fixtures.orphan.OrphanApi;
import io.github.doriangrelu.generator.fixtures.valid.billing.BillingApi;
import io.github.doriangrelu.generator.fixtures.valid.billing.deep.DeepBillingApi;
import io.github.doriangrelu.generator.fixtures.valid.catalog.CatalogApi;
import io.github.doriangrelu.generator.scan.ScannedContracts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiDomainResolverTest {

    private static final String BILLING_PKG = "io.github.doriangrelu.generator.fixtures.valid.billing";
    private static final String CATALOG_PKG = "io.github.doriangrelu.generator.fixtures.valid.catalog";
    private static final String NEST_OUTER_PKG = "io.github.doriangrelu.generator.fixtures.nesting.outer";
    private static final String NEST_INNER_PKG = "io.github.doriangrelu.generator.fixtures.nesting.outer.inner";
    private static final String DUP_ONE_PKG = "io.github.doriangrelu.generator.fixtures.dup.one";
    private static final String DUP_TWO_PKG = "io.github.doriangrelu.generator.fixtures.dup.two";
    private static final String BAD_ID_PKG = "io.github.doriangrelu.generator.fixtures.badid";

    private final ApiDomainResolver resolver = new ApiDomainResolver(getClass().getClassLoader());

    @Test
    @DisplayName("attributes each @ApiContract to its nearest @ApiDomain ancestor")
    void attributesContractsToNearestDomain() {
        final ScannedContracts scanned = new ScannedContracts(
                List.of(BillingApi.class, DeepBillingApi.class, CatalogApi.class),
                List.of(BILLING_PKG, CATALOG_PKG));

        final List<ResolvedDomain> domains = resolver.resolve(scanned);

        assertThat(domains).extracting(ResolvedDomain::id).containsExactly("billing", "catalog");
        assertThat(byId(domains, "billing").contractInterfaces())
                .containsExactlyInAnyOrder(BillingApi.class, DeepBillingApi.class);
        assertThat(byId(domains, "catalog").contractInterfaces())
                .containsExactly(CatalogApi.class);
    }

    @Test
    @DisplayName("keeps the raw @ApiDomain declaration, including a blank title")
    void keepsRawDomainDeclaration() {
        final ScannedContracts scanned = new ScannedContracts(List.of(), List.of(BILLING_PKG, CATALOG_PKG));

        final List<ResolvedDomain> domains = resolver.resolve(scanned);

        assertThat(byId(domains, "billing").declaration().info().title()).isEqualTo("Billing API");
        assertThat(byId(domains, "catalog").declaration().info().title()).isEmpty();
    }

    @Test
    void failsWhenAContractHasNoDomainAncestor() {
        final ScannedContracts scanned = new ScannedContracts(List.of(OrphanApi.class), List.of(BILLING_PKG));

        assertThatThrownBy(() -> resolver.resolve(scanned))
                .isInstanceOf(ApiContractConfigurationException.class)
                .hasMessageContaining("OrphanApi")
                .hasMessageContaining("not inside any @ApiDomain");
    }

    @Test
    void failsWhenADomainIsNestedInAnother() {
        final ScannedContracts scanned = new ScannedContracts(List.of(),
                List.of(NEST_OUTER_PKG, NEST_INNER_PKG));

        assertThatThrownBy(() -> resolver.resolve(scanned))
                .isInstanceOf(ApiContractConfigurationException.class)
                .hasMessageContaining("nested inside");
    }

    @Test
    void failsWhenTwoDomainsShareAnId() {
        final ScannedContracts scanned = new ScannedContracts(List.of(), List.of(DUP_ONE_PKG, DUP_TWO_PKG));

        assertThatThrownBy(() -> resolver.resolve(scanned))
                .isInstanceOf(ApiContractConfigurationException.class)
                .hasMessageContaining("dup-id")
                .hasMessageContaining("several packages");
    }

    @Test
    void failsWhenAnIdIsNotUrlSafe() {
        final ScannedContracts scanned = new ScannedContracts(List.of(), List.of(BAD_ID_PKG));

        assertThatThrownBy(() -> resolver.resolve(scanned))
                .isInstanceOf(ApiContractConfigurationException.class)
                .hasMessageContaining("Bad_Id")
                .hasMessageContaining("must match");
    }

    private static ResolvedDomain byId(final List<ResolvedDomain> domains, final String id) {
        return domains.stream().filter(domain -> domain.id().equals(id)).findFirst().orElseThrow();
    }
}
