package uk.gov.justice.digital.hmpps.riskprofiler.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PrisonMockServer

class MessageIntegrationTest : QueueIntegrationTest() {

  @Test
  fun `will transfer message from DLQ`() {
    val message = javaClass.getResource("/messages/incident.json")?.readText()

    awsDlqClientForEvents.sendMessage(dlqUrl, message)

    await untilCallTo { getNumberOfMessagesCurrentlyOnDLQueue() } matches { it == 1 }

    webTestClient.post()
      .uri("/batch-helper/transferEventMessages")
      .headers(setAuthorisation(roles = listOf("ROLE_RISK_PROFILER")))
      .exchange()
      .expectStatus().isOk
      .expectBody().isEmpty

    // message should arrive on normal Q and be processed
    await untilCallTo { PrisonMockServer.prisonMockServer.findAll(WireMock.getRequestedFor(WireMock.urlEqualTo("/api/incidents/3661338"))) } matches { it != null && it.isNotEmpty() }
  }
}
