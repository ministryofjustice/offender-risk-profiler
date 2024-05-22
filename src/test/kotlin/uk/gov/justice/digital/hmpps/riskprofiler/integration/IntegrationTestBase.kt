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
    inboundSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(inboundQueueUrl).build()).get()
    inboundSqsDlqClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(inboundDlqUrl).build()).get()
    outboundSqsClientSpy.purgeQueue(PurgeQueueRequest.builder().queueUrl(outboundQueueUrl).build()).get()
    outboundSqsDlqClientSpy.purgeQueue(PurgeQueueRequest.builder().queueUrl(outboundDlqUrl).build()).get()
    outboundTestSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(outboundTestQueueUrl).build()).get()
    outboundTestNoDlqSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(outboundTestNoDlqQueueUrl).build()).get()
  }

  fun HmppsSqsProperties.inboundQueueConfig() =
    queues["inboundqueue"] ?: throw MissingQueueException("inboundqueue has not been loaded from configuration properties")

  fun HmppsSqsProperties.outboundQueueConfig() =
    queues["outboundqueue"] ?: throw MissingQueueException("outboundqueue has not been loaded from configuration properties")

  fun HmppsSqsProperties.inboundTopicConfig() =
    topics["inboundtopic"] ?: throw MissingTopicException("inboundtopic has not been loaded from configuration properties")

  fun HmppsSqsProperties.outboundTopicConfig() =
    topics["outboundtopic"] ?: throw MissingTopicException("outboundtopic has not been loaded from configuration properties")

  private val inboundQueue by lazy { hmppsQueueService.findByQueueId("inboundqueue") ?: throw MissingQueueException("HmppsQueue inboundqueue not found") }
  private val outboundQueue by lazy { hmppsQueueService.findByQueueId("outboundqueue") ?: throw MissingQueueException("HmppsQueue outboundqueue not found") }
  private val outboundTestQueue by lazy { hmppsQueueService.findByQueueId("outboundtestqueue") ?: throw MissingQueueException("HmppsQueue outboundtestqueue not found") }
  private val inboundTopic by lazy { hmppsQueueService.findByTopicId("inboundtopic") ?: throw MissingQueueException("HmppsTopic inboundtopic not found") }
  private val outboundTestNoDlqQueue by lazy { hmppsQueueService.findByQueueId("outboundtestnodlqqueue") ?: throw MissingQueueException("HmppsQueue outboundtestnodlqqueue not found") }

  protected val inboundSqsClient by lazy { inboundQueue.sqsClient }
  protected val inboundSqsDlqClient by lazy { inboundQueue.sqsDlqClient as SqsAsyncClient }
  protected val inboundSnsClient by lazy { inboundTopic.snsClient }
  protected val outboundTestSqsClient by lazy { outboundTestQueue.sqsClient }
  protected val outboundTestNoDlqSqsClient by lazy { outboundTestNoDlqQueue.sqsClient }

  @SpyBean
  @Qualifier("outboundqueue-sqs-client")
  protected lateinit var outboundSqsClientSpy: SqsAsyncClient

  @SpyBean
  @Qualifier("outboundqueue-sqs-dlq-client")
  protected lateinit var outboundSqsDlqClientSpy: SqsAsyncClient

  protected val inboundQueueUrl by lazy { inboundQueue.queueUrl }
  protected val inboundDlqUrl by lazy { inboundQueue.dlqUrl as String }
  protected val outboundQueueUrl by lazy { outboundQueue.queueUrl }
  protected val outboundDlqUrl by lazy { outboundQueue.dlqUrl as String }
  protected val outboundTestQueueUrl by lazy { outboundTestQueue.queueUrl }
  protected val outboundTestNoDlqQueueUrl by lazy { outboundTestNoDlqQueue.queueUrl }

  protected val inboundTopicArn by lazy { inboundTopic.arn }

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

    @Bean("outboundqueue-sqs-client")
    fun outboundQueueSqsClient(
      hmppsSqsProperties: HmppsSqsProperties,
      @Qualifier("outboundqueue-sqs-dlq-client") outboundQueueSqsDlqClient: SqsAsyncClient,
    ): SqsAsyncClient =
      with(hmppsSqsProperties) {
        val config = queues["outboundqueue"] ?: throw MissingQueueException("HmppsSqsProperties config for outboundqueue not found")
        hmppsQueueFactory.createSqsAsyncClient(config, hmppsSqsProperties, outboundQueueSqsDlqClient)
      }

    @Bean("outboundqueue-sqs-dlq-client")
    fun outboundQueueSqsDlqClient(hmppsSqsProperties: HmppsSqsProperties): SqsAsyncClient =
      with(hmppsSqsProperties) {
        val config = queues["outboundqueue"] ?: throw MissingQueueException("HmppsSqsProperties config for outboundqueue not found")
        hmppsQueueFactory.createSqsAsyncDlqClient(config, hmppsSqsProperties)
      }
  }

  companion object {
    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun testcontainers(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }
  }
}
