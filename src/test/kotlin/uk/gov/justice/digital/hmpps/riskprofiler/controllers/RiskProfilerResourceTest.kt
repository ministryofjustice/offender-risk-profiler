package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.OAuthMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PathfinderMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PrisonMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PrisonerAlertsApiMockServer

class RiskProfilerResourceTest : ResourceTest() {

  @BeforeEach
  fun init() {
    OAuthMockServer.oauthMockServer.stubGrantToken()
    PrisonMockServer.prisonMockServer.stubBookingDetails(12)
    PrisonMockServer.prisonMockServer.stubOffender("A1234AB")
    PrisonerAlertsApiMockServer.prisonerAlertsApiMockServer.stubAlerts()
    PrisonMockServer.prisonMockServer.stubIncidents()
    PrisonMockServer.prisonMockServer.stubSentences(12)
    PrisonMockServer.prisonMockServer.stubMainOffence(12)
  }

  @Test
  fun testGetSoc() {
    val response = testRestTemplate.exchange(
      "/risk-profile/soc/A1234AB",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 200)
    assertThat(response.body)
      .isEqualTo("""{"nomsId":"A1234AB","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}""")
  }

  @Test
  fun testGetSocNoAuth() {
    val response = testRestTemplate.exchange(
      "/risk-profile/soc/A1234AC",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER-invalid", emptyList()),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 403)
  }

  @Test fun testGetSocSecurity() {
    val response = testRestTemplate.exchange(
      "/risk-profile/soc/A5015DY",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 200)
    assertThat(response.body)
      .isEqualTo("""{"nomsId":"A5015DY","provisionalCategorisation":"C","transferToSecurity":true,"riskType":"SOC"}""")
  }

  @Test
  fun testGetEscape() {
    val response = testRestTemplate.exchange(
      "/risk-profile/escape/A1234AB",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 200)
    assertThat(response.body)
      .isEqualTo("""{"nomsId":"A1234AB","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":true,"escapeRiskAlerts":[{"alertCode":"DUM","dateCreated":"2021-06-14","activeFrom":"2024-05-01","active":true,"expired":false},{"alertCode":"DUM","dateCreated":"2021-06-14","activeFrom":"2024-05-01","active":true,"expired":false}],"escapeListAlerts":[],"riskType":"ESCAPE"}""")
  }

  @Test
  fun testGetEscapeNoAuth() {
    val response = testRestTemplate.exchange(
      "/risk-profile/escape/A1234AC",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER-invalid", emptyList()),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 403)
  }

  @Test
  fun testGetViolence() {
    val response = testRestTemplate.exchange(
      "/risk-profile/violence/A1234AB",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 200)
    assertThat(response.body)
      .isEqualTo("""{"nomsId":"A1234AB","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":true,"numberOfAssaults":1,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}""")
  }

  @Test
  fun testGetViolenceNoAuth() {
    val response = testRestTemplate.exchange(
      "/risk-profile/violence/A1234AC",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER-invalid", emptyList()),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 403)
  }

  @Test
  fun testGetExtremism() {
    val response = testRestTemplate.exchange(
      "/risk-profile/extremism/A1234AB?previousOffences=true",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 200)
    assertThat(response.body)
      .isEqualTo("""{"nomsId":"A1234AB","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}""")
  }

  @Test
  fun testGetExtremismIsNominal() {
    PathfinderMockServer.pathfinderMockServer.stubPathfinder("A1234AB")

    val response = testRestTemplate.exchange(
      "/risk-profile/extremism/A1234AB?previousOffences=true",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 200)
    assertThat(response.body)
      .isEqualTo("""{"nomsId":"A1234AB","provisionalCategorisation":"B","notifyRegionalCTLead":true,"increasedRiskOfExtremism":true,"riskType":"EXTREMISM"}""")
  }

  @Test
  fun testGetExtremismNoAuth() {
    val response = testRestTemplate.exchange(
      "/risk-profile/extremism/A1234AC",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER-invalid", emptyList()),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 403)
  }

  @Test
  fun testGetLife() {
    val response = testRestTemplate.exchange(
      "/risk-profile/life/A1234AB",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER", RISK_PROFILER_ROLE),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 200)
    assertThat(response.body)
      .isEqualTo("""{"nomsId":"A1234AB","provisionalCategorisation":"B","life":true,"riskType":"LIFE"}""")
  }

  @Test
  fun testGetLifeNoAuth() {
    val response = testRestTemplate.exchange(
      "/risk-profile/life/A1234AC",
      HttpMethod.GET,
      createHttpEntityWithBearerAuthorisation("API_TEST_USER-invalid", emptyList()),
      object : ParameterizedTypeReference<String?>() {
      },
    )
    assertThatStatus(response, 403)
  }

  companion object {
    private val RISK_PROFILER_ROLE = listOf("ROLE_RISK_PROFILER")
  }
}
