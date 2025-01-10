package uk.gov.justice.digital.hmpps.riskprofiler.clent

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
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
      .bodyToFlux(Alert::class.java)
      .collect(Collectors.toList())
      .block()
  }
}