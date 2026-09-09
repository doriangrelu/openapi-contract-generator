package io.github.doriangrelu.generator.proxy;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractProxyFactoryTest {

    @RequestMapping("/sample")
    interface SampleApi {
        @GetMapping
        String list();
    }

    private final Object proxy = ContractProxyFactory.create(SampleApi.class, getClass().getClassLoader());

    @Test
    void createsAProxyImplementingTheInterface() {
        assertThat(proxy).isInstanceOf(SampleApi.class);
    }

    @Test
    void endpointMethodsThrowInsteadOfBeingInvoked() {
        assertThatThrownBy(((SampleApi) proxy)::list)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("no implementation");
    }

    @Test
    void objectMethodsBehaveSanely() {
        assertThat(proxy).hasToString("ApiContractProxy[" + SampleApi.class.getName() + "]");
        assertThat(proxy).isEqualTo(proxy);
        assertThat(proxy.hashCode()).isEqualTo(System.identityHashCode(proxy));
    }
}
