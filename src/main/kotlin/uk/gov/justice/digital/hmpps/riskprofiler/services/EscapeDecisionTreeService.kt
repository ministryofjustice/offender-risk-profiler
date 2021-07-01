package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile
import uk.gov.justice.digital.hmpps.riskprofiler.services.EscapeDecisionTreeService
import java.util.stream.Collectors
import javax.validation.constraints.NotNull

@Service
class EscapeDecisionTreeService(private val nomisService: NomisService) {
  fun getEscapeProfile(nomsId: @NotNull String?): EscapeProfile {
    val escapeData = nomisService.getEscapeListAlertsForOffender(nomsId)
    val splitLists = escapeData.stream().filter { alert -> alert.active!! }
      .collect(Collectors.partitioningBy { alert -> alert.alertCode == "XEL" })
    val escapeListAlerts = splitLists[true]!!
    val escapeRiskAlerts = splitLists[false]!!
    log.debug(
      "Escape profile for {}: {} list alerts, {} risk alerts",
      nomsId,
      escapeListAlerts.size,
      escapeRiskAlerts.size
    )
    return EscapeProfile(
      nomsId!!,
      RiskProfile.DEFAULT_CAT,
      !escapeListAlerts.isEmpty(),
      !escapeRiskAlerts.isEmpty(),
      escapeListAlerts,
      escapeRiskAlerts
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(EscapeDecisionTreeService::class.java)
  }
}
