package uk.gov.justice.digital.hmpps.riskprofiler.integration

import com.amazonaws.services.sqs.AmazonSQS
import com.google.gson.Gson
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.OAuthMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PathfinderMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PrisonMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.utils.JwtAuthenticationHelper
import java.time.Duration

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan
abstract class IntegrationTest {

  @SpyBean
  @Qualifier("awsClientForEvents")
  internal lateinit var awsSqsClient: AmazonSQS

  @Autowired
  private lateinit var gson: Gson

  @Autowired
  internal lateinit var webTestClient: WebTestClient

  @Autowired
  internal lateinit var jwtHelper: JwtAuthenticationHelper

  init {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken("user", "pw")
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  @BeforeEach
  fun resetStubs() {
    PrisonMockServer.prisonMockServer.resetAll()
    OAuthMockServer.oauthMockServer.resetAll()
    PathfinderMockServer.pathfinderMockServer.resetAll()
    OAuthMockServer.oauthMockServer.stubGrantToken()
    PrisonMockServer.prisonMockServer.stubIncidents()
  }

  internal fun Any.asJson() = gson.toJson(this)

  protected fun setAuthorisation(
    user: String = "prisoner-search-client",
    roles: List<String> = listOf(),
  ): (HttpHeaders) -> Unit {
    val token = jwtHelper.createJwt(
      subject = user,
      scope = listOf("read"),
      expiryTime = Duration.ofHours(1L),
      roles = roles,
    )
    return { it.set(HttpHeaders.AUTHORIZATION, "Bearer $token") }
  }
}
