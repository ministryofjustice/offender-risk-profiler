package uk.gov.justice.digital.hmpps.riskprofiler.clent

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsmanageprisonvisitsorchestration.dto.RestPage
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert
import java.util.stream.Collectors

@Service
class PrisonerAlertsApiClient(
  @Qualifier("prisonerAlertsSystemWebClient") private val webClient: WebClient,
) {

  fun findPrisonerAlerts(prisonerNumber: String, alertCodes: List<String>): List<Alert>? {
    val commaSeparatedAlertCodes = alertCodes.stream().collect(Collectors.joining(","))

    return webClient.get()
      .uri("/prisoners/$prisonerNumber/alerts?alertCodes=$commaSeparatedAlertCodes")
      .retrieve()
      .bodyToMono<RestPage<Alert>>()
      .block()!!
      .content
      .toList()
  }
}