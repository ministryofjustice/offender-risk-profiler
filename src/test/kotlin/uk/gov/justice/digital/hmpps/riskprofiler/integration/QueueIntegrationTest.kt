package uk.gov.justice.digital.hmpps.riskprofiler.integration

import com.amazonaws.services.sqs.AmazonSQS
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PrisonMockServer

@ActiveProfiles(profiles = ["test", "localstack"])
abstract class QueueIntegrationTest : IntegrationTest() {

  @Autowired
  lateinit var queueUrl: String

  @Autowired
  lateinit var dlqUrl: String

  @Autowired
  lateinit var awsDlqClientForEvents: AmazonSQS

  @Autowired
  lateinit var gson: Gson

  fun getNumberOfMessagesCurrentlyOnQueue(): Int? {
    val queueAttributes = awsSqsClient.getQueueAttributes(queueUrl, listOf("ApproximateNumberOfMessages"))
    return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
  }

  fun prisonRequestCountFor(url: String) = PrisonMockServer.prisonMockServer.findAll(getRequestedFor(urlEqualTo(url))).count()

  fun getNumberOfMessagesCurrentlyOnDLQueue(): Int? {
    val queueAttributes = awsDlqClientForEvents.getQueueAttributes(dlqUrl, listOf("ApproximateNumberOfMessages"))
    return queueAttributes.attributes["ApproximateNumberOfMessages"]?.toInt()
  }

  fun prisonSearch(prisonId: String, fileAssert: String) {
    webTestClient.get().uri("/prisoner-search/prison/$prisonId")
      .headers(setAuthorisation(roles = listOf("ROLE_GLOBAL_SEARCH")))
      .header("Content-Type", "application/json")
      .exchange()
      .expectStatus().isOk
      .expectBody().json(fileAssert.readResourceAsText())
  }

  fun prisonSearchPagination(prisonId: String, size: Long, page: Long, fileAssert: String) {
    webTestClient.get().uri("/prisoner-search/prison/$prisonId?size=$size&page=$page")
      .headers(setAuthorisation(roles = listOf("ROLE_GLOBAL_SEARCH")))
      .header("Content-Type", "application/json")
      .exchange()
      .expectStatus().isOk
      .expectBody().json(fileAssert.readResourceAsText())
  }
}

private fun String.readResourceAsText(): String = QueueIntegrationTest::class.java.getResource(this).readText()
