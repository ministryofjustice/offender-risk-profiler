package uk.gov.justice.digital.hmpps.riskprofiler.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
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
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.OAuthExtension
import uk.gov.justice.digital.hmpps.riskprofiler.integration.testcontainers.LocalStackContainer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.testcontainers.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.hmpps.sqs.HmppsQueueFactory
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsSqsProperties
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.MissingTopicException

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestBase.SqsConfig::class, JwtAuthHelper::class)
@ExtendWith(OAuthExtension::class)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @BeforeEach
  fun `clear queues`() {
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
  protected lateinit var objectMapper: ObjectMapper

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @SpyBean
  protected lateinit var hmppsSqsPropertiesSpy: HmppsSqsProperties

  @Autowired
  lateinit var webTestClient: WebTestClient

  internal fun HttpHeaders.authToken(roles: List<String> = listOf("ROLE_QUEUE_ADMIN")) {
    this.setBearerAuth(
      jwtAuthHelper.createJwt(
        subject = "SOME_USER",
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
