package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile
import java.util.stream.Collectors

@Service
class EscapeDecisionTreeService(private val nomisService: NomisService) {
  fun getEscapeProfile(nomsId: String): EscapeProfile {
    val escapeData = nomisService.getEscapeListAlertsForOffender(nomsId)
    val splitLists = escapeData.stream().filter { alert -> alert.active && !alert.isExpired() }
      .collect(Collectors.partitioningBy { alert -> alert.alertCode == "XEL" })
    val escapeListAlerts = splitLists[true]!!
    val escapeRiskAlerts = splitLists[false]!!
    log.debug("Escape profile for $nomsId: ${escapeListAlerts.size} list alerts, ${escapeRiskAlerts.size} risk alerts")
    return EscapeProfile(
      nomsId,
      RiskProfile.DEFAULT_CAT,
      escapeListAlerts.isNotEmpty(),
      escapeRiskAlerts.isNotEmpty(),
      escapeRiskAlerts,
      escapeListAlerts,
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(EscapeDecisionTreeService::class.java)
  }
}
