package uk.gov.justice.digital.hmpps.riskprofiler.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RiskProfilerResourceTest extends ResourceTest {

    private static final List<String> RISK_PROFILER_ROLE = List.of("ROLE_RISK_PROFILER");

    @Test
    public void testGetSoc() {

        final var response = testRestTemplate.exchange(
                "/risk-profile/soc/A1234AB",
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "testGetSoc.json");
    }

    @Test
    public void testGetSocNoAuth() {

        final var response = testRestTemplate.exchange(
                "/risk-profile/soc/A1234AC",
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER-invalid", Collections.emptyList()),
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, 403);
    }

    @Test
    public void testGetSocSecurity() {

        final var response = testRestTemplate.exchange(
                "/risk-profile/soc/A5015DY",
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, 200);
        assertThat(response.getBody()).isEqualTo("{\"nomsId\":\"A5015DY\",\"provisionalCategorisation\":\"C\",\"transferToSecurity\":true,\"riskType\":\"SOC\"}");
    }
}
