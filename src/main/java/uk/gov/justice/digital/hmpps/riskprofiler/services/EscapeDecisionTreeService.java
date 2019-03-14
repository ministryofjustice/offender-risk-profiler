package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile;

import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EscapeDecisionTreeService {

    private final NomisService nomisService;

    public EscapeDecisionTreeService(NomisService nomisService) {
        this.nomisService = nomisService;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public EscapeProfile getEscapeProfile(@NotNull final String nomsId) {
        log.debug("Calculating escape profile for {}", nomsId);
        var escapeData = nomisService.getEscapeListAlertsForOffender(nomsId);

        final var splitLists =
                escapeData.stream().filter(Alert::isActive).collect(Collectors.partitioningBy(a -> a.getAlertCode().equals("XEL")));

        var escapeListAlerts = splitLists.get(true);
        var escapeRiskAlerts = splitLists.get(false);
        return EscapeProfile.escapeBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(RiskProfile.DEFAULT_CAT)
                .activeEscapeList(!escapeListAlerts.isEmpty())
                .activeEscapeRisk(!escapeRiskAlerts.isEmpty())
                .escapeListAlerts(escapeListAlerts)
                .escapeRiskAlerts(escapeRiskAlerts)
                .build();
    }
}
