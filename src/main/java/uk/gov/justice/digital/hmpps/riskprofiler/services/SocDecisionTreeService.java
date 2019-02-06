package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.OcgRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.OcgmRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrasRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

import static uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile.DEFAULT_CAT;

@Service
public class SocDecisionTreeService {

    private final DataRepository<Pras> repository;
    private final OcgmRepository ocgmRepository;
    private final OcgRepository ocgRepository;
    private final NomisService nomisService;

    private final Set<String> OCGM_BANDS = Set.of("1a", "1b", "1c", "2a", "2b", "2c", "3a", "3b", "3c");

    public SocDecisionTreeService(PrasRepository repository, OcgmRepository ocgmRepository, OcgRepository ocgRepository, NomisService nomisService) {
        this.repository = repository;
        this.ocgmRepository = ocgmRepository;
        this.ocgRepository = ocgRepository;
        this.nomisService = nomisService;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public SocProfile getSocData(@NotNull final String nomsId) {

        var soc = SocProfile.socBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(DEFAULT_CAT);

        var prasData = repository.getByKey(nomsId);

        if (prasData.isPresent()) {
            soc.transferToSecurity(true);
            soc.provisionalCategorisation("C");
        } else {

            ocgmRepository.getByKey(nomsId)
                    .ifPresent(ocgm -> ocgRepository.getByKey(ocgm.getOcgId()).ifPresentOrElse(ocg -> {

                //Check OCGM Band = 1a, 1b, 1c, 2a, 2b, 2c, 3a, 3b, 3c?
                if (OCGM_BANDS.contains(ocg.getOcgmBand())) {
                    soc.provisionalCategorisation("C");
                    if ("Principal Subject" .equalsIgnoreCase(ocgm.getStandingWithinOcg())) {
                        soc.transferToSecurity(true);
                    }
                } else {
                    if ("Principal Subject" .equalsIgnoreCase(ocgm.getStandingWithinOcg())) {
                        soc.transferToSecurity(true);
                        soc.provisionalCategorisation("C");
                    } else {
                        if (isHasActiveSocAlerts(nomsId)) {
                            // TODO: NOT MVP - we will trigger a notification to security
                            soc.provisionalCategorisation("C");
                        } else {
                            soc.provisionalCategorisation("C");
                        }
                    }
                }
            }, () -> {
                if (isHasActiveSocAlerts(nomsId)) {
                    // TODO: NOT MVP - we will trigger a notification to security
                    soc.provisionalCategorisation("C");
                } else {
                    soc.provisionalCategorisation(DEFAULT_CAT);
                }
            }));
        }

        return soc.build();

    }

    private boolean isHasActiveSocAlerts(@NotNull String nomsId) {
        // TODO: Check NOMIS: Does the offender have any active SOC alerts active within last 12 months?
        var socAlerts = nomisService.getAlertsForOffender(nomsId, "SOC");

        return socAlerts.stream()
                .anyMatch(alert -> alert.isActive() && alert.getDateCreated().isAfter(LocalDate.now().minusYears(1)));
    }
}
