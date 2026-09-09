package io.github.doriangrelu.generator;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.doriangrelu.generator.spring.OpenApiGeneratorApplication;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml31;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base test that generates one OpenAPI 3.1 document per {@code @ApiDomain} package found on
 * the classpath. An API contract module enables generation by adding a single empty
 * subclass in {@code src/test/java}:
 *
 * <pre>{@code
 * class GenerateOpenApiSpecs extends io.github.doriangrelu.generator.OpenApiSpecGeneratorTest {
 * }
 * }</pre>
 *
 * <p>The context starts with {@link SpringBootTest.WebEnvironment#MOCK} — no port is
 * opened. For each springdoc group, {@code GET /v3/api-docs/{group}} is written to
 * {@code <outputDir>/<group>.json}, and the same document is re-serialised to
 * {@code <group>.yaml}. The output directory is the value of the
 * {@value #OUTPUT_DIR_PROPERTY} system property, or {@code target/openapi} by default
 * (which resolves under the module running the build).
 *
 * <p>The test asserts only that each document was produced (HTTP&nbsp;200 and a non-empty
 * file). It intentionally does not assert anything about the document's content; validating
 * the contract itself (golden file, linting, breaking-change diff) is the consumer's
 * responsibility.
 */
@SpringBootTest(
        classes = OpenApiGeneratorApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.main.web-application-type=servlet",
                "spring.main.banner-mode=off",
                "springdoc.api-docs.version=openapi_3_1",
                "springdoc.writer-with-order-by-keys=true",
                "springdoc.writer-with-default-pretty-printer=true"
        })
public abstract class OpenApiSpecGeneratorTest {

    /** System property selecting the output directory (default {@code target/openapi}). */
    public static final String OUTPUT_DIR_PROPERTY = "openapi.output.dir";

    private static final Path DEFAULT_OUTPUT_DIR = Path.of("target", "openapi");
    private static final String GROUP_DOCS_PATH = "/v3/api-docs/{group}";
    private static final Logger LOG = System.getLogger(OpenApiSpecGeneratorTest.class.getName());

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired(required = false)
    private List<GroupedOpenApi> groups = List.of();

    @Test
    void generatesOneDocumentPerApiDomain() throws Exception {
        assertThat(groups)
                .as("no @ApiDomain package was found on the classpath")
                .isNotEmpty();

        final MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        final Path outputDir = resolveOutputDir();
        Files.createDirectories(outputDir);

        for (final GroupedOpenApi group : groups) {
            final String json = mockMvc.perform(get(GROUP_DOCS_PATH, group.getGroup()))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            final Path jsonFile = outputDir.resolve(group.getGroup() + ".json");
            final Path yamlFile = outputDir.resolve(group.getGroup() + ".yaml");
            Files.writeString(jsonFile, json);
            Files.writeString(yamlFile, toYaml(json));

            LOG.log(Level.INFO, "@ApiDomain ''{0}'' -> {1}, {2}",
                    group.getGroup(), jsonFile.toAbsolutePath(), yamlFile.toAbsolutePath());

            assertThat(jsonFile).isNotEmptyFile();
            assertThat(yamlFile).isNotEmptyFile();
        }
    }

    private static Path resolveOutputDir() {
        final String configured = System.getProperty(OUTPUT_DIR_PROPERTY);
        return configured != null ? Path.of(configured) : DEFAULT_OUTPUT_DIR;
    }

    private static String toYaml(final String json) throws Exception {
        final JsonNode tree = Json31.mapper().readTree(json);
        return Yaml31.mapper().writeValueAsString(tree);
    }
}
