package uk.gov.justice.digital.hmpps.riskprofiler.controllers

import org.assertj.core.api.Assertions
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
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
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.OAuthMockServer.Companion.oauthMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PathfinderMockServer.Companion.pathfinderMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PrisonMockServer.Companion.prisonMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService
import uk.gov.justice.digital.hmpps.riskprofiler.utils.JwtAuthenticationHelper
import java.time.Duration
import java.util.Objects
import java.util.UUID

@ActiveProfiles(profiles = ["test", "localstack"])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
abstract class ResourceTest {

  @Autowired
  protected lateinit var dataRepositoryFactory: DataRepositoryFactory

  @Autowired
  protected lateinit var testRestTemplate: TestRestTemplate

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthenticationHelper

  @Autowired
  protected lateinit var nomisService: NomisService

  fun createHttpEntityWithBearerAuthorisation(user: String?, roles: List<String>?): HttpEntity<*> {
    val jwt = createJwt(user, roles)
    return createHttpEntity(jwt, null)
  }

  private fun allFilesLoaded(): Boolean {
    return dataRepositoryFactory.getRepositories().stream().allMatch { it.dataAvailable() }
  }

  @BeforeEach
  fun resetStubs() {
    nomisService.evictSocListAlertsCache("A1234AB")
    nomisService.evictSocListAlertsCache("A1234AC")
    nomisService.evictSocListAlertsCache("A1234AE")
    nomisService.evictSocListAlertsCache("A5015DY")
    nomisService.evictIncidentsCache("A1234AB")
    nomisService.evictIncidentsCache("A1234AC")
    nomisService.evictEscapeListAlertsCache("A1234AB")
    nomisService.evictEscapeListAlertsCache("A1234AC")

    prisonMockServer.resetAll()
    oauthMockServer.resetAll()
    pathfinderMockServer.resetAll()
    await until { allFilesLoaded() }
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
    Assertions.assertThat(response.statusCode).withFailMessage(
      "Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s",
      response.statusCode,
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
