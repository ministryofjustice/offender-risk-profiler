package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

import static uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile.DEFAULT_CAT;

@Service
@Slf4j
public class SocDecisionTreeService {

    public static final String PRINCIPAL_SUBJECT = "Principal Subject";
    private final DataRepositoryFactory repositoryFactory;
    private final NomisService nomisService;

    private final Set<String> OCGM_BANDS = Set.of("1a", "1b", "1c", "2a", "2b", "2c", "3a", "3b", "3c");

    public SocDecisionTreeService(DataRepositoryFactory factory, NomisService nomisService) {
        this.repositoryFactory = factory;
        this.nomisService = nomisService;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public SocProfile getSocData(@NotNull final String nomsId) {
        log.debug("Calculating SOC profile for {}", nomsId);

        var soc = buildSocProfile(nomsId);

        var prasData = repositoryFactory.getRepository(Pras.class).getByKey(nomsId);

        if (prasData.isPresent()) {
            log.debug("SOC: {} is present in PRAS", nomsId);
            soc.transferToSecurity(true);
            soc.provisionalCategorisation("C");
        } else {

            repositoryFactory.getRepository(OcgmList.class).getByKey(nomsId)
                    .ifPresentOrElse(
                            ocgmSet -> {
                                log.debug("SOC: {} present in OGCM list", nomsId);
                                ocgmSet.getData()
                                        .stream().map(ocgm -> {
                                    var potentialProfile = buildSocProfile(nomsId);
                                    repositoryFactory.getRepository(Ocg.class).getByKey(ocgm.getOcgId())
                                            .ifPresentOrElse(ocg -> checkBand(nomsId, potentialProfile, ocgm, ocg),
                                                    () -> checkAlerts(nomsId, potentialProfile, DEFAULT_CAT));

                                    return potentialProfile.build();
                                }).sorted()
                                        .findFirst().ifPresent(s -> {
                                    soc.transferToSecurity(s.isTransferToSecurity());
                                    soc.provisionalCategorisation(s.getProvisionalCategorisation());
                                });
                            },
                            () -> checkAlerts(nomsId, soc, DEFAULT_CAT));
        }

        return soc.build();

    }

    private SocProfile.SocProfileBuilder buildSocProfile(@NotNull String nomsId) {
        return SocProfile.socBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(DEFAULT_CAT);
    }

    private void checkBand(@NotNull String nomsId, SocProfile.SocProfileBuilder soc, Ocgm ocgm, Ocg ocg) {
        //Check OCGM Band = 1a, 1b, 1c, 2a, 2b, 2c, 3a, 3b, 3c?
        if (OCGM_BANDS.contains(ocg.getOcgmBand())) {
            log.debug("SOC: {} in OGCM band 1 to 3", nomsId);
            soc.provisionalCategorisation("C");
            if (PRINCIPAL_SUBJECT.equalsIgnoreCase(ocgm.getStandingWithinOcg())) {
                log.debug("SOC: {} in OGCM band 1 to 3 and principal subject", nomsId);
                soc.transferToSecurity(true);
            }
        } else {
            log.debug("SOC: {} not in OGCM band 1 to 3", nomsId);
            if (PRINCIPAL_SUBJECT.equalsIgnoreCase(ocgm.getStandingWithinOcg())) {
                log.debug("SOC: {} principal subject", nomsId);
                soc.transferToSecurity(true);
                soc.provisionalCategorisation("C");
            } else {
                checkAlerts(nomsId, soc, "C");
            }
        }
    }

    private void checkAlerts(@NotNull String nomsId, SocProfile.SocProfileBuilder soc, String defaultCat) {
        if (isHasActiveSocAlerts(nomsId)) {
            log.debug("SOC: active alerts for {}", nomsId);
            // TODO: NOT MVP - we will trigger a notification to security
            soc.provisionalCategorisation("C");
        } else {
            log.debug("SOC: no active alerts for {}", nomsId);
            soc.provisionalCategorisation(defaultCat);
        }
    }

    private boolean isHasActiveSocAlerts(String nomsId) {
        return nomisService.getSocListAlertsForOffender(nomsId).stream()
                .anyMatch(alert -> alert.isActive() &&
                        alert.getDateCreated().isAfter(LocalDate.now().minusYears(1)));
    }
}
