package uk.gov.justice.digital.hmpps.riskprofiler.integration

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks.PrisonMockServer

@ActiveProfiles(profiles = ["test", "localstack"])
abstract class QueueIntegrationTest : IntegrationTest() {

  fun prisonRequestCountFor(url: String) = PrisonMockServer.prisonMockServer.findAll(getRequestedFor(urlEqualTo(url))).count()

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
