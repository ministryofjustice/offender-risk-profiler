package uk.gov.justice.digital.hmpps.riskprofiler.integration.health

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import uk.gov.justice.digital.hmpps.riskprofiler.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.OAuthExtension.Companion.oAuthApi
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueHealth
import uk.gov.justice.hmpps.sqs.HmppsSqsProperties
import java.net.URI

@Import(QueueHealthCheckNegativeTest.TestConfig::class)
class QueueHealthCheckNegativeTest : IntegrationTestBase() {

  @TestConfiguration
  class TestConfig {
    @Bean
    fun badQueueHealth(hmppsConfigProperties: HmppsSqsProperties): HmppsQueueHealth {
      val sqsClient = SqsAsyncClient.builder()
        .endpointOverride(URI.create(hmppsConfigProperties.localstackUrl))
        .region(Region.of(hmppsConfigProperties.region))
        .credentialsProvider(AnonymousCredentialsProvider.create())
        .build()
      return HmppsQueueHealth(HmppsQueue("missingQueueId", sqsClient, "missingQueue", sqsClient, "missingDlq"))
    }
  }

  @Test
  fun `Queue health down`() {
    oAuthApi.stubHealthPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody()
      .jsonPath("status").isEqualTo("DOWN")
      .jsonPath("components.badQueueHealth.status").isEqualTo("DOWN")
      .jsonPath("components.badQueueHealth.details.queueName").isEqualTo("missingQueue")
      .jsonPath("components.badQueueHealth.details.dlqName").isEqualTo("missingDlq")
      .jsonPath("components.badQueueHealth.details.dlqStatus").isEqualTo("DOWN")
      .jsonPath("components.badQueueHealth.details.error").exists()
  }
}
