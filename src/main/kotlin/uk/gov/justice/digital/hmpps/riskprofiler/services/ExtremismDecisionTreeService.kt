package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder
import uk.gov.justice.digital.hmpps.riskprofiler.model.ExtremismProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile
import java.util.Optional
import javax.validation.constraints.NotNull

@Service
class ExtremismDecisionTreeService(private val repository: PathfinderService) {
  fun getExtremismProfile(nomsId: @NotNull String, previousOffences: Boolean?): ExtremismProfile {
    val pathFinder = repository.getBand(nomsId)
    return decisionProcess(nomsId, java.lang.Boolean.TRUE == previousOffences, pathFinder)
  }

  private fun decisionProcess(
    nomsId: String,
    previousOffences: Boolean,
    pathFinder: Optional<PathFinder>
  ): ExtremismProfile {
    val extremism = ExtremismProfile(nomsId = nomsId, provisionalCategorisation = RiskProfile.DEFAULT_CAT)
    pathFinder.ifPresent { pf: PathFinder ->
      val banding = pf.pathFinderBanding
      log.info("extremism: {} in pathfinder on {}, increased Risk of Extremism", nomsId, banding)
      if (banding == null) {
        extremism.provisionalCategorisation = "C"
      } else if (banding == 1 || banding == 2) {
        extremism.increasedRiskOfExtremism = true
        extremism.notifyRegionalCTLead = true
        if (previousOffences) {
          log.info("extremism: {} has previous offences", nomsId)
          extremism.provisionalCategorisation = "B"
        } else {
          extremism.provisionalCategorisation = "C"
        }
      } else if (banding == 3) {
        extremism.notifyRegionalCTLead = true
        extremism.provisionalCategorisation = "C"
      } else {
        extremism.provisionalCategorisation = "C"
      }
    }
    return extremism
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
