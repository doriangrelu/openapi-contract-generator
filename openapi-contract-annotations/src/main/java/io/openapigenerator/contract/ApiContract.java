package io.openapigenerator.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Marks a Java interface as an <em>HTTP API contract</em>: a set of endpoints described
 * with Spring Web annotations ({@code @GetMapping}, {@code @PathVariable}, …) but with no
 * implementation in the module.
 *
 * <p>The annotation is meta-annotated with {@link RestController @RestController} and
 * {@link RequestMapping @RequestMapping} so that, at generation time, a documentation-only
 * proxy of the interface is recognised by Spring MVC and by springdoc exactly like a
 * hand-written {@code @RestController}. Nothing needs to be added to the interface beyond
 * this marker and the usual mapping annotations.
 *
 * <h2>Base path</h2>
 * A base path may be given directly on the marker:
 * <pre>{@code
 * @ApiContract("/invoices")
 * public interface InvoiceApi {
 *     @GetMapping("/{id}")
 *     Invoice getInvoice(@PathVariable("id") String id);
 * }
 * }</pre>
 * This is equivalent to {@code @ApiContract @RequestMapping("/invoices")}. Do not use both
 * forms on the same type.
 *
 * <h2>Domain</h2>
 * Every {@code @ApiContract} interface must live (directly or transitively) inside a
 * package annotated with {@link ApiDomain @ApiDomain}; otherwise generation fails. The
 * interface contributes its operations to that domain's OpenAPI document.
 *
 * @see ApiDomain
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping
public @interface ApiContract {

    /**
     * Base path for every operation declared on the interface. Aliased to
     * {@link RequestMapping#path()}.
     *
     * @return zero or more path prefixes (usually exactly one)
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] value() default {};
}
