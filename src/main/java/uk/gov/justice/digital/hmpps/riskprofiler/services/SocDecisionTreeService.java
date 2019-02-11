package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

import static uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile.DEFAULT_CAT;

@Service
public class SocDecisionTreeService {

    private final DataRepositoryFactory repositoryFactory;
    private final NomisService nomisService;

    private final Set<String> OCGM_BANDS = Set.of("1a", "1b", "1c", "2a", "2b", "2c", "3a", "3b", "3c");

    public SocDecisionTreeService(DataRepositoryFactory factory, NomisService nomisService) {
        this.repositoryFactory = factory;
        this.nomisService = nomisService;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public SocProfile getSocData(@NotNull final String nomsId) {

        var soc = SocProfile.socBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(DEFAULT_CAT);

        var prasData = repositoryFactory.getRepository(Pras.class).getByKey(nomsId);

        if (prasData.isPresent()) {
            soc.transferToSecurity(true);
            soc.provisionalCategorisation("C");
        } else {

            repositoryFactory.getRepository(Ocgm.class).getByKey(nomsId)
                    .ifPresentOrElse(
                            ocgm -> repositoryFactory.getRepository(Ocg.class).getByKey(ocgm.getOcgId())
                                    .ifPresentOrElse(ocg -> {

                                        //Check OCGM Band = 1a, 1b, 1c, 2a, 2b, 2c, 3a, 3b, 3c?
                                        if (OCGM_BANDS.contains(ocg.getOcgmBand())) {
                                            soc.provisionalCategorisation("C");
                                            if ("Principal Subject".equalsIgnoreCase(ocgm.getStandingWithinOcg())) {
                                                soc.transferToSecurity(true);
                                            }
                                        } else {
                                            if ("Principal Subject".equalsIgnoreCase(ocgm.getStandingWithinOcg())) {
                                                soc.transferToSecurity(true);
                                                soc.provisionalCategorisation("C");
                                            } else {
                                                checkAlerts(nomsId, soc, "C");
                                            }
                                        }
                                    }, () -> checkAlerts(nomsId, soc, DEFAULT_CAT)),
                            () -> checkAlerts(nomsId, soc, DEFAULT_CAT));
        }

        return soc.build();

    }

    private void checkAlerts(@NotNull String nomsId, SocProfile.SocProfileBuilder soc, String defaultCat) {
        if (isHasActiveSocAlerts(nomsId)) {
            // TODO: NOT MVP - we will trigger a notification to security
            soc.provisionalCategorisation("C");
        } else {
            soc.provisionalCategorisation(defaultCat);
        }
    }

    private boolean isHasActiveSocAlerts(String nomsId) {
        return nomisService.getSocListAlertsForOffender(nomsId).stream()
                .anyMatch(alert -> alert.isActive() &&
                        alert.getDateCreated().isAfter(LocalDate.now().minusYears(1)));
    }
}
