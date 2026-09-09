package io.github.doriangrelu.generator.openapi;

import java.util.List;
import java.util.Map;

import io.github.doriangrelu.contract.ApiDomain;
import io.github.doriangrelu.generator.ApiContractConfigurationException;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenApiMetadataMapperTest {

    private ApiDomain meta;
    private ApiDomain minimal;
    private ApiDomain schemeWithoutName;
    private ApiDomain schemeWithoutType;

    @BeforeAll
    void loadFixtures() {
        meta = readDomain("io.github.doriangrelu.generator.fixtures.meta");
        minimal = readDomain("io.github.doriangrelu.generator.fixtures.valid.catalog");
        schemeWithoutName = readDomain("io.github.doriangrelu.generator.fixtures.metabad");
        schemeWithoutType = readDomain("io.github.doriangrelu.generator.fixtures.metanotype");
    }

    @Test
    void mapsInfoWithContactAndLicense() {
        final Info info = OpenApiMetadataMapper.toInfo(meta.info(), "fallback");

        assertThat(info.getTitle()).isEqualTo("Meta API");
        assertThat(info.getVersion()).isEqualTo("3.2.1");
        assertThat(info.getTermsOfService()).isEqualTo("https://meta.example.com/tos");
        assertThat(info.getContact().getEmail()).isEqualTo("meta@example.com");
        assertThat(info.getLicense().getName()).isEqualTo("Apache-2.0");
        assertThat(info.getLicense().getIdentifier()).isEqualTo("Apache-2.0");
    }

    @Test
    void fillsBlankTitleAndVersionWithDefaults() {
        final Info info = OpenApiMetadataMapper.toInfo(minimal.info(), "catalog");

        assertThat(info.getTitle()).isEqualTo("catalog");
        assertThat(info.getVersion()).isEqualTo("0.0.1");
        assertThat(info.getContact()).isNull();
        assertThat(info.getLicense()).isNull();
    }

    @Test
    void mapsServerVariables() {
        final List<Server> servers = OpenApiMetadataMapper.toServers(meta.servers());

        assertThat(servers).singleElement().satisfies(server -> {
            assertThat(server.getUrl()).isEqualTo("https://{region}.meta.example.com");
            assertThat(server.getVariables().get("region").getDefault()).isEqualTo("eu");
            assertThat(server.getVariables().get("region").getEnum()).containsExactly("eu", "us");
        });
    }

    @Test
    void mapsHttpApiKeyAndOauthSecuritySchemes() {
        final Map<String, SecurityScheme> schemes =
                OpenApiMetadataMapper.toSecuritySchemes(meta.securitySchemes());

        assertThat(schemes).containsOnlyKeys("bearerAuth", "apiKey", "oauth");
        assertThat(schemes.get("bearerAuth").getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(schemes.get("bearerAuth").getScheme()).isEqualTo("bearer");
        assertThat(schemes.get("bearerAuth").getBearerFormat()).isEqualTo("JWT");
        assertThat(schemes.get("apiKey").getType()).isEqualTo(SecurityScheme.Type.APIKEY);
        assertThat(schemes.get("apiKey").getIn()).isEqualTo(SecurityScheme.In.HEADER);
        assertThat(schemes.get("apiKey").getName()).isEqualTo("X-API-Key");
        assertThat(schemes.get("oauth").getFlows().getAuthorizationCode().getScopes())
                .containsKeys("read", "write");
    }

    @Test
    void rejectsSecuritySchemeWithoutName() {
        final var schemes = schemeWithoutName.securitySchemes();

        assertThatThrownBy(() -> OpenApiMetadataMapper.toSecuritySchemes(schemes))
                .isInstanceOf(ApiContractConfigurationException.class)
                .hasMessageContaining("must be set");
    }

    @Test
    void rejectsSecuritySchemeWithoutType() {
        final var schemes = schemeWithoutType.securitySchemes();

        assertThatThrownBy(() -> OpenApiMetadataMapper.toSecuritySchemes(schemes))
                .isInstanceOf(ApiContractConfigurationException.class)
                .hasMessageContaining("must declare a type");
    }

    private static ApiDomain readDomain(final String packageName) {
        try {
            return Class.forName(packageName + ".package-info").getAnnotation(ApiDomain.class);
        }
        catch (final ClassNotFoundException ex) {
            throw new IllegalStateException("missing fixture: " + packageName, ex);
        }
    }
}
