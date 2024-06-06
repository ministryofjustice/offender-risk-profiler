package uk.gov.justice.digital.hmpps.riskprofiler.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PathfinderMockServer.Companion.pathfinderMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PrisonMockServer.Companion.prisonMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.ResourceOAuthMockServer.Companion.oauthMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService

import uk.gov.justice.hmpps.sqs.HmppsQueueFactory
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsSqsProperties
import uk.gov.justice.hmpps.sqs.MissingQueueException

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestBase.SqsConfig::class, JwtAuthHelper::class)
//@ExtendWith(OAuthExtension::class)
@ActiveProfiles("test")
@TestPropertySource(locations= ["classpath:test.properties"])
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var nomisService: NomisService

  @Autowired
  protected lateinit var dataRepositoryFactory: DataRepositoryFactory

  private fun allFilesLoaded(): Boolean {
    return dataRepositoryFactory.getRepositories().stream().allMatch { it.dataAvailable() }
  }

  @BeforeEach
  fun `clear queues`() {

      nomisService.evictSocListAlertsCache("A1234AB")
      nomisService.evictSocListAlertsCache("A1234AC")
      nomisService.evictSocListAlertsCache("A1234AE")
      nomisService.evictSocListAlertsCache("A5015DY")
      nomisService.evictIncidentsCache("A1234AB")
      nomisService.evictIncidentsCache("A1234AC")
      nomisService.evictEscapeListAlertsCache("A1234AB")
      nomisService.evictEscapeListAlertsCache("A1234AC")

   //   prisonMockServer.resetAll()
  //    oauthMockServer.resetAll()
  //    pathfinderMockServer.resetAll()
      await until { allFilesLoaded() }

    riskProfilerChangeSqsClientSpy.purgeQueue(PurgeQueueRequest.builder().queueUrl(riskProfilerChangeQueueUrl).build()).get()
    riskProfilerChangeSqsDlqClientSpy.purgeQueue(PurgeQueueRequest.builder().queueUrl(riskProfilerChangeDlqUrl).build()).get()
 }

  private val riskProfilerChangeQueue by lazy { hmppsQueueService.findByQueueId("riskprofilechangequeue") ?: throw MissingQueueException("HmppsQueue outboundqueue not found") }

  protected val riskProfilerChangeQueueUrl by lazy { riskProfilerChangeQueue.queueUrl }
  protected val riskProfilerChangeDlqUrl by lazy { riskProfilerChangeQueue.dlqUrl as String }

  fun HmppsSqsProperties.riskProfilerChangeQueueConfig() =
    queues["riskprofilechangequeue"] ?: throw MissingQueueException("riskprofilechangequeue has not been loaded from configuration properties")

  @SpyBean
  @Qualifier("riskprofilechangequeue-sqs-client")
  protected lateinit var riskProfilerChangeSqsClientSpy: SqsAsyncClient

  @SpyBean
  @Qualifier("riskprofilechangequeue-sqs-dlq-client")
  protected lateinit var riskProfilerChangeSqsDlqClientSpy: SqsAsyncClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @SpyBean
  protected lateinit var hmppsSqsPropertiesSpy: HmppsSqsProperties

  @Autowired
  lateinit var webTestClient: WebTestClient

  internal fun HttpHeaders.authToken(user: String?, roles: List<String> = listOf("ROLE_QUEUE_ADMIN", "ROLE_RISK_PROFILER")) {
    this.setBearerAuth(
      jwtAuthHelper.createJwt(
        subject = user,
        roles = roles,
        clientId = "some-client",
      ),
    )
  }

  protected fun gsonString(any: Any) = Gson().toJson(any) as String

  @TestConfiguration
  class SqsConfig(private val hmppsQueueFactory: HmppsQueueFactory) {

    @Bean("riskprofilechangequeue-sqs-client")
    fun riskProfileChangeQueueSqsClient(
      hmppsSqsProperties: HmppsSqsProperties,
      @Qualifier("riskprofilechangequeue-sqs-dlq-client") riskProfileChangeQueueSqsDlqClient: SqsAsyncClient,
    ): SqsAsyncClient =
      with(hmppsSqsProperties) {
        val config = queues["riskprofilechangequeue"] ?: throw MissingQueueException("HmppsSqsProperties config for hmppsriskprofilechangequeue not found")
        hmppsQueueFactory.createSqsAsyncClient(config, hmppsSqsProperties, riskProfileChangeQueueSqsDlqClient)
      }

    @Bean("riskprofilechangequeue-sqs-dlq-client")
    fun riskProfileChangeQueueSqsDlqClient(hmppsSqsProperties: HmppsSqsProperties): SqsAsyncClient =
      with(hmppsSqsProperties) {
        val config = queues["riskprofilechangequeue"] ?: throw MissingQueueException("HmppsSqsProperties config for outboundqueue not found")
        hmppsQueueFactory.createSqsAsyncDlqClient(config, hmppsSqsProperties)
      }
  }
}
