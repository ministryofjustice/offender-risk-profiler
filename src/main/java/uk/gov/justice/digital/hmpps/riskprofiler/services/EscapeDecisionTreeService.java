package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
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

    public EscapeDecisionTreeService(final NomisService nomisService) {
        this.nomisService = nomisService;
    }

    public EscapeProfile getEscapeProfile(@NotNull final String nomsId) {
        final var escapeData = nomisService.getEscapeListAlertsForOffender(nomsId);

        final var splitLists =
                escapeData.stream().filter(Alert::isActive).collect(Collectors.partitioningBy(a -> a.getAlertCode().equals("XEL")));

        final var escapeListAlerts = splitLists.get(true);
        final var escapeRiskAlerts = splitLists.get(false);
        log.debug("Escape profile for {}: {} list alerts, {} risk alerts", nomsId, escapeListAlerts.size(), escapeRiskAlerts.size());
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
