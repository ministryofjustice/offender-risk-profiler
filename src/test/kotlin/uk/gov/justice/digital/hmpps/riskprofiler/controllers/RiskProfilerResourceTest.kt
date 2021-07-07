package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.OAuthMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PrisonMockServer

class RiskProfilerResourceTest : ResourceTest() {

  @BeforeEach
  fun init() {
    OAuthMockServer.oauthMockServer.stubGrantToken()
    PrisonMockServer.prisonMockServer.stubAlerts()
  }

  @Test
  fun testGetSoc() {
    val response = testRestTemplate.exchange(
      "/risk-profile/soc/A1234AB",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      }
    )
    assertThatJsonFileAndStatus(response, 200, "testGetSoc.json")
  }

  @Test
  fun testGetSocNoAuth() {
    val response = testRestTemplate.exchange(
      "/risk-profile/soc/A1234AC",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER-invalid", emptyList()),
      object : ParameterizedTypeReference<String?>() {
      }
    )
    assertThatStatus(response, 403)
  }

  @Test
  fun testGetSocSecurity() {
    val response = testRestTemplate.exchange(
      "/risk-profile/soc/A5015DY",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      }
    )
    assertThatStatus(response, 200)
    Assertions.assertThat(response.body)
      .isEqualTo("{\"nomsId\":\"A5015DY\",\"provisionalCategorisation\":\"C\",\"transferToSecurity\":true,\"riskType\":\"SOC\"}")
  }

  companion object {
    private val RISK_PROFILER_ROLE = listOf("ROLE_RISK_PROFILER")
  }
}
