package uk.gov.justice.digital.hmpps.riskprofiler.controllers;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.List;

public class RiskProfilerResourceTest extends ResourceTest { // ************** TODO needs wiremock or similar, see case notes api

    private static final List<String> RISK_PROFILER_ROLE = List.of("ROLE_RISK_PROFILER");

    // @Test
    public void testGetSoc() {

        final var response = testRestTemplate.exchange(
                "/risk-profile/soc/A1234AA",
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "testGetSoc.json");
    }

    // @Test
    public void testGetSocDoesNotExist() {

        final var response = testRestTemplate.exchange(
                "/risk-profile/soc/A1234ZZ",
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, 404);
    }

    @Test
    public void testGetSocNoAuth() {

        final var response = testRestTemplate.exchange(
                "/risk-profile/soc/A1234AA",
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", Collections.emptyList()),
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, 403);
    }
}
