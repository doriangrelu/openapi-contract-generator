package io.openapigenerator.generator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Creates <em>documentation-only</em> proxies for {@code @ApiContract} interfaces.
 *
 * <p>Spring MVC supports {@code @RequestMapping} declared on an interface when the bean is
 * an interface-based (JDK) proxy of that interface — which is exactly what this factory
 * produces. springdoc then documents the proxy like any {@code @RestController}. The
 * proxied endpoint methods are never invoked; calling one throws
 * {@link UnsupportedOperationException}.
 */
final class ContractProxyFactory {

    private ContractProxyFactory() {
    }

    /**
     * @param contractInterface a {@code @ApiContract}-annotated interface
     * @param classLoader       the class loader that defined {@code contractInterface}
     * @return a JDK proxy implementing {@code contractInterface}
     */
    static Object create(Class<?> contractInterface, ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, new Class<?>[] {contractInterface},
                new DocumentationOnlyHandler(contractInterface));
    }

    private record DocumentationOnlyHandler(Class<?> contractInterface) implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "toString" -> "ApiContractProxy[" + contractInterface.getName() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null ? null : args[0]);
                default -> throw new UnsupportedOperationException(
                        "@ApiContract interface " + contractInterface.getName()
                        + " has no implementation; this proxy exists only for OpenAPI generation.");
            };
        }
    }
}
