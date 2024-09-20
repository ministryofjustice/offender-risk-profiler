package uk.gov.justice.digital.hmpps.riskprofiler.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.riskprofiler.controllers.ResourceTest
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PathfinderMockServer
import uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock.PrisonMockServer

class HealthIntTest : ResourceTest() {

  @BeforeEach
  fun init() {
    PrisonMockServer.prisonMockServer.stubPing()
    PathfinderMockServer.pathfinderMockServer.stubPing()
  }

  @Test
  fun `Health page reports ok`() {
    webTestClient.get().uri("/health")
      .exchange()
      .expectStatus().isOk
    // .expectBody().jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health liveness page is accessible`() {
    webTestClient.get().uri("/health/liveness")
      .exchange()
      .expectStatus().isOk
    // .expectBody().jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health readiness page is accessible`() {
    webTestClient.get().uri("/health/readiness")
      .exchange()
      .expectStatus().isOk
    //  .expectBody().jsonPath("status").isEqualTo("UP")
  }
}
