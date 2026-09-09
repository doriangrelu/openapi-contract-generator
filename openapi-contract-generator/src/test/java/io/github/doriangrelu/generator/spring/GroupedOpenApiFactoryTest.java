package io.github.doriangrelu.generator.spring;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import io.github.doriangrelu.generator.proxy.ContractProxyFactory;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.assertj.core.api.Assertions.assertThat;

class GroupedOpenApiFactoryTest {

    @RequestMapping("/member")
    interface MemberApi {
        @GetMapping
        String list();
    }

    interface NonMemberApi {
        @GetMapping
        String list();
    }

    private static final Set<Class<?>> MEMBERS = Set.of(MemberApi.class);

    @Test
    void matchesAMethodDeclaredOnAMemberInterface() throws NoSuchMethodException {
        final Method method = MemberApi.class.getMethod("list");

        assertThat(GroupedOpenApiFactory.belongsTo(method, MEMBERS)).isTrue();
    }

    @Test
    void matchesAProxyMethodWhoseInterfaceIsAMember() {
        final Object proxy = ContractProxyFactory.create(MemberApi.class, getClass().getClassLoader());
        final Method proxyMethod = methodNamed(proxy.getClass(), "list");

        assertThat(proxyMethod.getDeclaringClass()).isNotEqualTo(MemberApi.class);
        assertThat(GroupedOpenApiFactory.belongsTo(proxyMethod, MEMBERS)).isTrue();
    }

    @Test
    void doesNotMatchANonMemberInterface() throws NoSuchMethodException {
        final Method method = NonMemberApi.class.getMethod("list");

        assertThat(GroupedOpenApiFactory.belongsTo(method, MEMBERS)).isFalse();
    }

    private static Method methodNamed(final Class<?> type, final String name) {
        return Arrays.stream(type.getMethods())
                .filter(method -> method.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
