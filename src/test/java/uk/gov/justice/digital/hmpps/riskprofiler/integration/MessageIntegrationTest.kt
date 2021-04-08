package uk.gov.justice.digital.hmpps.riskprofiler.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test

class MessageIntegrationTest : QueueIntegrationTest() {

  @Test
  fun `will transfer message from DLQ`() {

    val message = "/messages/incident.json".readResourceAsText()

    awsDlqClientForEvents.sendMessage(dlqUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnDLQueue() } matches { it == 1 }

    webTestClient.post()
      .uri("/batch-helper/transferEventMessages")
      .headers(setAuthorisation(roles = listOf("ROLE_RISK_PROFILER")))
      .exchange()
      .expectStatus().isOk
      .expectBody().isEmpty

    // message should arrive on normal Q and be processed
    await untilCallTo { prisonMockServer.findAll(WireMock.getRequestedFor(WireMock.urlEqualTo("/api/incidents/3661338"))) } matches { it != null && it.isNotEmpty() }
  }
}

private fun String.readResourceAsText(): String {
  return MessageIntegrationTest::class.java.getResource(this).readText()
}
