package uk.gov.justice.digital.hmpps.riskprofiler.clent

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert.PrisonerAlertResponseDto
import uk.gov.justice.digital.hmpps.riskprofiler.model.RestPage
import java.util.stream.Collectors

@Service
class PrisonerAlertsApiClient(
  @Qualifier("prisonerAlertsSystemWebClient") private val webClient: WebClient,
) {

  fun findPrisonerAlerts(prisonerId: String, alertCodes: List<String>): RestPage<PrisonerAlertResponseDto> {
    return getPrisonerAlertsAsMono(prisonerId, alertCodes).block()
      ?: throw IllegalStateException("Unable to retrieve alerts for prisoner, possibly due to timeout $prisonerId")
  }

  private fun getPrisonerAlertsAsMono(prisonerNumber: String, alertCodes: List<String>): Mono<RestPage<PrisonerAlertResponseDto>> {
    val commaSeparatedAlertCodes = alertCodes.stream().collect(Collectors.joining(","))
    val uri = "/prisoners/$prisonerNumber/alerts?alertCode=$commaSeparatedAlertCodes"

    return webClient.get()
      .uri(uri)
      .retrieve()
      .bodyToMono<RestPage<PrisonerAlertResponseDto>>()
      .doOnError { e ->
        log.error("getPrisonerAlertsAsMono Failed for get request $uri, exception - $e")
      }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}