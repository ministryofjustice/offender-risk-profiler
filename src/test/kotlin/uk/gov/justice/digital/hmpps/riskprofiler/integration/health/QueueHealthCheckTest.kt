package uk.gov.justice.digital.hmpps.riskprofiler.integration.health

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.riskprofiler.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PathfinderMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PathfinderMockServer.Companion.pathfinderMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PrisonMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PrisonMockServer.Companion.prisonMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.ResourceOAuthMockServer

class QueueHealthCheckTest : IntegrationTestBase() {
  @Test
  fun `risk profiler change queue health ok`() {

    //oAuthApi.stubHealthPing(200)

    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .consumeWith(System.out::println)
      .jsonPath("status").isEqualTo("UP")
      .jsonPath("components.riskprofilechangequeue-health.status").isEqualTo("UP")
      .jsonPath("components.riskprofilechangequeue-health.details.queueName").isEqualTo(hmppsSqsPropertiesSpy.riskProfilerChangeQueueConfig().queueName)
  }
}
