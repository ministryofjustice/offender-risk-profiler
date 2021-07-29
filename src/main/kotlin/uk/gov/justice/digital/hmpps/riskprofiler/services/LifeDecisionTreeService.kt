package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.model.LifeProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile
import java.util.Locale

@Service
class LifeDecisionTreeService(private val nomisService: NomisService) {
  fun getLifeProfile(nomsId: String): LifeProfile {
    val bookingId = nomisService.getBooking(nomsId)
    val lifeFlag = isLifeFlag(bookingId)
    val lifeStatus = isLifeStatus(bookingId)
    val murder = isMurder(bookingId)
    val life = lifeFlag || lifeStatus || murder
    val cat = if (life) "B" else RiskProfile.DEFAULT_CAT
    log.info("Life result for {}: {} (lifeFlag={} lifeStatus={} murder={})", nomsId, life, lifeFlag, lifeStatus, murder)
    return LifeProfile(nomsId, cat, life)
  }

  private fun isLifeFlag(bookingId: Long): Boolean {
    val sentenceData = nomisService.getSentencesForOffender(bookingId)
    return sentenceData.stream()
      .anyMatch { oft -> java.lang.Boolean.TRUE == oft.lifeSentence }
  }

  private fun isLifeStatus(bookingId: Long): Boolean {
    val imprisonmentData = nomisService.getBookingDetails(bookingId)
    return imprisonmentData.stream().anyMatch { (_, _, _, imprisonmentStatus) ->
      imprisonmentStatus != null && LIFE_STATUS.contains(
        imprisonmentStatus
      )
    }
  }

  private fun isMurder(bookingId: Long): Boolean {
    val mainOffence = nomisService.getMainOffences(bookingId)
    return mainOffence.stream().anyMatch { it.uppercase(Locale.getDefault()).startsWith("MURDER") }
  }

  companion object {
    private val log = LoggerFactory.getLogger(LifeDecisionTreeService::class.java)

    /**
     * These values come from the population management information IMPRISONMENT_STATUS_SHORT field (JSAS calculated) as used by policy.
     * They actually refer to codes in the IMPRISONMENT_STATUSES Nomis table.
     */
    private val LIFE_STATUS = listOf(
      "ALP", "ALP_LASPO", "CFLIFE", "DFL", "DLP", "DIED", "HMPL",
      "LIFE", "MLP", "SEC90_03", "SEC93", "SEC93_03", "SEC94", "SEC19_3B"
    )
  }
}
