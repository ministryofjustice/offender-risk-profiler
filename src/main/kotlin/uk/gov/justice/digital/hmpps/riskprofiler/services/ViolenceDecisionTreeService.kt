package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.ViperRepository
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Viper
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentResponse
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.ViolenceProfile
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
class ViolenceDecisionTreeService(
  viperDataRepository: ViperRepository,
  nomisService: NomisService,
  @Value("\${app.assaults.min:5}") minNumAssaults: Int,
  @Value("\${app.assaults.check.months:12}") months: Int,
  @Value("\${app.viper-threshold:5.00}") viperScoreThreshold: BigDecimal,
) {
  private val viperScoreThreshold: BigDecimal
  private val minNumAssaults: Int
  private val months: Int
  private val viperDataRepository: DataRepository<Viper>
  private val nomisService: NomisService

  private fun recentAssaults(assault: IncidentCase): Boolean {
    return assault.reportTime != null && assault.reportTime!!.compareTo(
      LocalDateTime.now().minusMonths(months.toLong()),
    ) >= 0
  }

  fun getViolenceProfile(nomsId: String): ViolenceProfile {
    // Check NOMIS Have the individuals had 5 or more assaults in custody? (remove DUPS)
    val assaults = nomisService.getIncidents(nomsId).stream()
      .filter { i: IncidentCase -> "DUP" != i.incidentStatus }.collect(Collectors.toList())

    // Check NOMIS: Have they had a serious assault in custody in past 12 months
    val numberOfSeriousAssaults = assaults.stream()
      .filter(this::recentAssaults)
      .filter { assault: IncidentCase ->
        assault.responses!!.stream()
          .anyMatch { response: IncidentResponse ->
            SERIOUS_ASSAULT_QUESTIONS.stream().anyMatch { saq: SeriousQuestionAndResponse -> isSerious(response, saq) }
          }
      }
      .count()
    val numberOfNonSeriousAssaults = assaults.stream()
      .filter(this::recentAssaults)
      .count() - numberOfSeriousAssaults
    val violenceProfile = ViolenceProfile(
      nomsId,
      RiskProfile.DEFAULT_CAT,
      false,
      false,
      !assaults.isEmpty(),
      assaults.size.toLong(),
      numberOfSeriousAssaults,
      numberOfNonSeriousAssaults,
    )
    viperDataRepository.getByKey(nomsId).ifPresentOrElse(
      { viper ->
        if (viper.score!!.compareTo(viperScoreThreshold) > 0) {
          log.debug("violence: Viper score {} above threshold for {}", viper.score, nomsId)
          violenceProfile.notifySafetyCustodyLead = true
          if (assaults.size >= minNumAssaults) {
            log.debug("violence: Viper assaults above threshold for {}", nomsId)
            if (numberOfSeriousAssaults > 0) {
              log.debug("violence: Viper serious assaults above threshold for {}", nomsId)
              violenceProfile.provisionalCategorisation = "B"
            } else {
              log.debug("violence: Viper serious assaults below threshold for {}", nomsId)
              violenceProfile.provisionalCategorisation = "C"
            }
          } else {
            log.debug("violence: Viper assaults below threshold {} for {}", minNumAssaults, nomsId)
            violenceProfile.provisionalCategorisation = "C"
          }
        } else {
          log.debug(
            "violence: Viper score {} is below threshold of {} for {}",
            viper.score,
            viperScoreThreshold,
            nomsId,
          )
          violenceProfile.provisionalCategorisation = RiskProfile.DEFAULT_CAT
        }
      },
    ) {
      log.debug("violence: No Viper score for {}", nomsId)
      violenceProfile.provisionalCategorisation = "C"
    }
    return violenceProfile
  }

  private fun isSerious(
    incidentResponse: IncidentResponse,
    seriousQuestionAndResponse: SeriousQuestionAndResponse,
  ): Boolean {
    return seriousQuestionAndResponse.question.equals(
      incidentResponse.question,
      ignoreCase = true,
    ) && seriousQuestionAndResponse.needAnswer.equals(incidentResponse.answer, ignoreCase = true)
  }

  private data class SeriousQuestionAndResponse(val question: String, val needAnswer: String)

  companion object {
    private val log = LoggerFactory.getLogger(ViolenceDecisionTreeService::class.java)
    private val SERIOUS_ASSAULT_QUESTIONS = listOf(
      SeriousQuestionAndResponse("WAS THIS A SEXUAL ASSAULT", "YES"),
      SeriousQuestionAndResponse("WAS MEDICAL TREATMENT FOR CONCUSSION OR INTERNAL INJURIES REQUIRED", "YES"),
      SeriousQuestionAndResponse("WAS A SERIOUS INJURY SUSTAINED", "YES"),
      SeriousQuestionAndResponse("DID INJURIES RESULT IN DETENTION IN OUTSIDE HOSPITAL AS AN IN-PATIENT", "YES"),
    )
  }

  init {
    this.viperDataRepository = viperDataRepository
    this.nomisService = nomisService
    this.minNumAssaults = minNumAssaults
    this.months = months
    this.viperScoreThreshold = viperScoreThreshold
  }
}
