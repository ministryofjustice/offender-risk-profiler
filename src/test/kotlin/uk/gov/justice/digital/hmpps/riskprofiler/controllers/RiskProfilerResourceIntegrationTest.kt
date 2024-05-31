package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.riskprofiler.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PathfinderMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PrisonMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.ResourceOAuthMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.ResourceOAuthMockServer.Companion.oauthMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.utils.JwtAuthenticationHelper
import java.time.Duration
import java.util.*

class RiskProfilerResourceIntegrationTest : IntegrationTestBase() {

  fun createHttpEntityWithBearerAuthorisation(user: String?, roles: List<String>?): HttpEntity<*> {
    val jwt = createJwt(user, roles)
    return createHttpEntity(jwt, null)
  }

  fun createHttpEntity(bearerToken: String, body: Any?): HttpEntity<*> {
    val headers = HttpHeaders()
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer $bearerToken")
    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
    if (body != null) {
      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }
    return HttpEntity(body, headers)
  }
  @Autowired
  protected lateinit var jwtAuthHelper2: JwtAuthenticationHelper

  fun createJwt(user: String?, roles: List<String>?): String {
    return jwtAuthHelper2.createJwt(
      user,
      java.util.List.of("read", "write"),
      roles,
      Duration.ofHours(1),
      UUID.randomUUID().toString()
    )
  }

  @BeforeEach
  fun init() {
    oauthMockServer.stubGrantToken()
    PathfinderMockServer.pathfinderMockServer.stubPathfinder("A1234AB")
    PrisonMockServer.prisonMockServer.stubBookingDetails(12)
    PrisonMockServer.prisonMockServer.stubOffender("A1234AB")
    PrisonMockServer.prisonMockServer.stubAlerts()
    PrisonMockServer.prisonMockServer.stubIncidents()
    PrisonMockServer.prisonMockServer.stubSentences(12)
    PrisonMockServer.prisonMockServer.stubMainOffence(12)
  }

  @Test
  fun testGetSoc() {
    webTestClient.get()
      .uri("/risk-profile/soc/A1234AB")
      .headers { it.authToken("API_TEST_USER-invalid", roles = RISK_PROFILER_ROLE) }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .consumeWith(System.out::println)
      .json("""{"nomsId":"A1234AB","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}""")
  }

  @Test
  fun testGetSocNoAuth() {
    webTestClient.get()
      .uri("/risk-profile/soc/A1234AC")
      .headers { it.authToken("API_TEST_USER-invalid", emptyList()) }
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun testGetEscapeNoAuth() {
    webTestClient.get()
      .uri("/risk-profile/escape/A1234AC")
      .headers { it.authToken("API_TEST_USER-invalid", emptyList()) }
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun testGetEscape() {
    webTestClient.get()
      .uri("/risk-profile/escape/A1234AB")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      //.headers{it.authToken(roles = listOf("ROLE_GLOBAL_SEARCH"))}
      .headers { it.authToken("API_TEST_USER", roles = RISK_PROFILER_ROLE) }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .consumeWith(System.out::println)
      .json("""{"nomsId":"A1234AB","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":true,"escapeRiskAlerts":[{"alertCode":"DUM","dateCreated":"2021-06-14","expired":false,"active":true},{"alertCode":"DUM","dateCreated":"2021-06-14","expired":false,"active":true}],"escapeListAlerts":[],"riskType":"ESCAPE"}""")
  }

  @Test
  fun testGetExtremism() {
    webTestClient.get()
      .uri("/risk-profile/extremism/A1234AB?previousOffences=true")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      //.headers{it.authToken(roles = listOf("ROLE_GLOBAL_SEARCH"))}
      .headers { it.authToken("API_TEST_USER", roles = RISK_PROFILER_ROLE) }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .consumeWith(System.out::println)
      .json("""{"nomsId":"A1234AB","provisionalCategorisation":"C","notifyRegionalCTLead":false,"increasedRiskOfExtremism":false,"riskType":"EXTREMISM"}""")
  }

  @Test
  fun testGetLife() {
    webTestClient.get()
      .uri("/risk-profile/life/A1234AB")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      //.headers{it.authToken(roles = listOf("ROLE_GLOBAL_SEARCH"))}
      .headers { it.authToken("API_TEST_USER", roles = RISK_PROFILER_ROLE) }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .consumeWith(System.out::println)
      .json("""{"nomsId":"A1234AB","provisionalCategorisation":"B","life":true,"riskType":"LIFE"}""")
  }

  @Test
  fun testGetLifeNoAuth() {
    webTestClient.get()
      .uri("/risk-profile/life/A1234AC")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      .headers { it.authToken("API_TEST_USER-invalid", emptyList()) }
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test fun testGetSocSecurity() {
    webTestClient.get()
      .uri("/risk-profile/soc/A5015DY")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      //.headers{it.authToken(roles = listOf("ROLE_GLOBAL_SEARCH"))}
      .headers { it.authToken("API_TEST_USER", roles = RISK_PROFILER_ROLE) }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .consumeWith(System.out::println)
      .json("""{"nomsId":"A5015DY","provisionalCategorisation":"C","transferToSecurity":true,"riskType":"SOC"}""")
  }

  @Test
  fun testGetViolence() {
    webTestClient.get()
      .uri("/risk-profile/violence/A1234AB")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      //.headers{it.authToken(roles = listOf("ROLE_GLOBAL_SEARCH"))}
      .headers { it.authToken("API_TEST_USER", roles = RISK_PROFILER_ROLE) }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .consumeWith(System.out::println)
      .json("""{"nomsId":"A1234AB","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":true,"numberOfAssaults":1,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}""")
  }

  @Test
  fun testGetViolenceNoAuth() {
    webTestClient.get()
      .uri("/risk-profile/violence/A1234AC")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      .headers { it.authToken("API_TEST_USER-invalid", emptyList()) }
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun testGetExtremismIsNominal() {
    PathfinderMockServer.pathfinderMockServer.stubPathfinder("A1234AB")

    webTestClient.get()
      .uri("/risk-profile/extremism/A1234AB?previousOffences=true")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      //.headers{it.authToken(roles = listOf("ROLE_GLOBAL_SEARCH"))}
      .headers { it.authToken("API_TEST_USER", roles = RISK_PROFILER_ROLE) }
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .consumeWith(System.out::println)
      .json("""{"nomsId":"A1234AB","provisionalCategorisation":"B","notifyRegionalCTLead":true,"increasedRiskOfExtremism":true,"riskType":"EXTREMISM"}""")
  }

  @Test
  fun testGetExtremismNoAuth() {
    webTestClient.get()
      .uri("/risk-profile/extremism/A1234AC")
      //.headers(createHttpEntityWithBearerAuthorisation("API_TEST_USER", RiskProfilerResourceTest.RISK_PROFILER_ROLE)),
      .headers { it.authToken("API_TEST_USER-invalid", emptyList()) }
      .exchange()
      .expectStatus()
      .isForbidden
  }

  companion object {
    private val RISK_PROFILER_ROLE = listOf("ROLE_RISK_PROFILER", "API_TEST_USER")
  }
}
