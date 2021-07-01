package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.json.JsonContent
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.OAuthMockServer.Companion.oauthMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PrisonMockServer.Companion.prisonMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService
import uk.gov.justice.digital.hmpps.riskprofiler.utils.JwtAuthenticationHelper
import java.time.Duration
import java.util.Objects
import java.util.UUID

@ActiveProfiles(profiles = ["test", "localstack"])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
abstract class ResourceTest {
  // @JvmField
  @Autowired
  protected lateinit var testRestTemplate: TestRestTemplate

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthenticationHelper

  @Autowired
  protected lateinit var nomisService: NomisService

  fun createHttpEntityWithBearerAuthorisation(user: String?, roles: List<String>?): HttpEntity<*> {
    val jwt = createJwt(user, roles)
    return createHttpEntity(jwt, null)
  }

  @BeforeEach
  fun resetStubs() {
    nomisService.evictSocListAlertsCache("A1234AB")
    nomisService.evictSocListAlertsCache("A1234AE")
    nomisService.evictSocListAlertsCache("A5015DY")
    prisonMockServer.resetAll()
    oauthMockServer.resetAll()
    oauthMockServer.stubGrantToken()
    prisonMockServer.stubAlerts()
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

  fun assertThatStatus(response: ResponseEntity<String?>, status: Int) {
    Assertions.assertThat(response.statusCodeValue).withFailMessage(
      "Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s",
      response.statusCodeValue,
      status,
      response.body
    ).isEqualTo(status)
  }

  fun assertThatJsonFileAndStatus(response: ResponseEntity<String?>, status: Int, jsonFile: String?) {
    assertThatStatus(response, status)
    Assertions.assertThat(getBodyAsJsonContent<Any>(response)).isEqualToJson(jsonFile)
  }

  private fun <T> getBodyAsJsonContent(response: ResponseEntity<String?>): JsonContent<T> {
    return JsonContent(javaClass, ResolvableType.forType(String::class.java), Objects.requireNonNull(response.body))
  }

  fun createJwt(user: String?, roles: List<String>?): String {
    return jwtAuthHelper.createJwt(
      user,
      java.util.List.of("read", "write"),
      roles,
      Duration.ofHours(1),
      UUID.randomUUID().toString()
    )
  }
}
