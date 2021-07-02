package uk.gov.justice.digital.hmpps.riskprofiler.integration

// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
// import uk.gov.justice.digital.hmpps.oauth2server.uk.gov.justice.digital.hmpps.config.TokenVerificationClientCredentials
// import uk.gov.justice.digital.hmpps.oauth2server.utils.JwtAuthHelper
// import uk.gov.justice.digital.hmpps.oauth2server.utils.JwtAuthHelper.JwtParameters

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// @Import(JwtAuthHelper::class)
// @ExtendWith(TokenVerificationExtension::class)
abstract class IntegrationTestCase {
  //    @Autowired
//    lateinit var webTestClient: WebTestClient
  lateinit var webTestClient: WebTestClient

//    @Autowired
//    private lateinit var jwtAuthHelper: JwtAuthHelper

//    @Autowired
//    private lateinit var deliusApiRestTemplate: OAuth2RestTemplate

  @LocalServerPort
  private var localServerPort: Int = 0

  internal lateinit var baseUrl: String

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

//  @BeforeEach
//  internal fun setupPort() {
//    baseUrl = "http://localhost:${localServerPort}"
//    // need to override port as random port only assigned on server startup
//    // (tokenVerificationApiRestTemplate.resource as TokenVerificationClientCredentials).accessTokenUri = "http://localhost:${localServerPort}/auth/oauth/token"
//    webTestClient = WebTestClient.bindToServer().baseUrl(baseUrl).build()
//  }

//    internal fun setAuthorisation(user: String, roles: List<String> = listOf()): (org.springframework.http.HttpHeaders) -> Unit {
//        val token = createJwt(user, roles)
//        return { it.set(HttpHeaders.AUTHORIZATION, "Bearer $token") }
//    }

  internal fun setBasicAuthorisation(token: String): (HttpHeaders) -> Unit =
    { it.set(HttpHeaders.AUTHORIZATION, "Basic $token") }

//    private fun createJwt(user: String, roles: List<String> = listOf()) =
//            jwtAuthHelper.createJwt(
//                    JwtParameters(username = user,
//                            scope = listOf("read", "write"),
//                            expiryTime = Duration.ofHours(1L),
//                            roles = roles))

  internal fun String.readFile(): String = this@IntegrationTestCase::class.java.getResource(this).readText()
}
