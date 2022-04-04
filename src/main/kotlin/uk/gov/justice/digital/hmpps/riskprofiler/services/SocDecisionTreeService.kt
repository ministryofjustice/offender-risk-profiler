package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Service
class SocDecisionTreeService(
  private val repositoryFactory: DataRepositoryFactory,
  private val nomisService: NomisService
) {
  private val OCGM_BANDS = setOf("1a", "1b", "1c", "2a", "2b", "2c", "3a", "3b", "3c")

  fun getSocData(nomsId: String): SocProfile {
    val soc = buildSocProfile(nomsId)
    val prasData = repositoryFactory.getRepository(Pras::class.java).getByKey(nomsId)
    if (prasData.isPresent) {
      log.debug("SOC: {} is present in PRAS", nomsId)
      soc.transferToSecurity = true
      soc.provisionalCategorisation = "C"
    } else {
      repositoryFactory.getRepository(OcgmList::class.java).getByKey(nomsId)
        .ifPresentOrElse(
          { ocgmSet: OcgmList ->
            log.debug("SOC: {} present in OGCM list", nomsId)
            ocgmSet.data
              .stream().map { ocgm: Ocgm ->
                val potentialProfile = buildSocProfile(nomsId)
                repositoryFactory.getRepository(Ocg::class.java).getByKey(ocgm.ocgId)
                  .ifPresentOrElse(
                    { ocg: Ocg -> checkBand(nomsId, potentialProfile, ocgm, ocg) }
                  ) { checkAlerts(nomsId, potentialProfile, RiskProfile.DEFAULT_CAT) }
                potentialProfile
              }.sorted()
              .findFirst().ifPresent { (_, provisionalCategorisation, transferToSecurity) ->
                soc.transferToSecurity = transferToSecurity
                soc.provisionalCategorisation = provisionalCategorisation
              }
          }
        ) { checkAlerts(nomsId, soc, RiskProfile.DEFAULT_CAT) }
    }
    return soc
  }

  private fun buildSocProfile(nomsId: String): SocProfile {
    return SocProfile(nomsId, RiskProfile.DEFAULT_CAT, false)
  }

  private fun checkBand(nomsId: @NotNull String?, soc: SocProfile, ocgm: Ocgm, ocg: Ocg) {
    // Check OCGM Band = 1a, 1b, 1c, 2a, 2b, 2c, 3a, 3b, 3c?
    // If band info is missing, we should assume it is effectively 5c
    if (ocg.ocgmBand != null && OCGM_BANDS.contains(ocg.ocgmBand)) {
      log.debug("SOC: {} in OGCM band 1 to 3", nomsId)
      soc.provisionalCategorisation = "C"
      if (ocgm.standingWithinOcg?.contains(PRINCIPAL_SUBJECT, ignoreCase = true) == true) {
        log.debug("SOC: {} in OGCM band 1 to 3 and principal subject", nomsId)
        soc.transferToSecurity = true
      }
    } else {
      log.debug("SOC: {} not in OGCM band 1 to 3", nomsId)
      if (ocgm.standingWithinOcg?.contains(PRINCIPAL_SUBJECT, ignoreCase = true) == true) {
        log.debug("SOC: {} principal subject", nomsId)
        soc.transferToSecurity = true
        soc.provisionalCategorisation = "C"
      } else {
        checkAlerts(nomsId, soc, "C")
      }
    }
  }

  private fun checkAlerts(nomsId: @NotNull String?, soc: SocProfile, defaultCat: String) {
    if (isHasActiveSocAlerts(nomsId)) {
      log.debug("SOC: active alerts for {}", nomsId)
      // TODO: NOT MVP - we will trigger a notification to security
      soc.provisionalCategorisation = "C"
    } else {
      log.debug("SOC: no active alerts for {}", nomsId)
      soc.provisionalCategorisation = defaultCat
    }
  }

  private fun isHasActiveSocAlerts(nomsId: String?): Boolean {
    return nomisService.getSocListAlertsForOffender(nomsId).stream()
      .anyMatch { alert -> alert.active && !alert.expired && alert.dateCreated.isAfter(LocalDate.now().minusYears(1)) }
  }

  companion object {
    private val log = LoggerFactory.getLogger(SocDecisionTreeService::class.java)
    const val PRINCIPAL_SUBJECT = "Principal Subject"
  }
}
