package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfile;

import javax.validation.constraints.NotNull;

@Service
@Slf4j
public class EscapeDecisionTreeService {

    private final NomisService nomisService;

    public EscapeDecisionTreeService(NomisService nomisService) {
        this.nomisService = nomisService;
    }

    @PreAuthorize("hasRole('RISK_PROFILER')")
    public EscapeProfile getEscapeProfile(@NotNull final String nomsId) {
        log.debug("Calculating escape profile for  {}", nomsId);
        var escapeData = nomisService.getEscapeListAlertsForOffender(nomsId);

        if(escapeData.isEmpty() || escapeData.get().isEmpty()){
            return EscapeProfile.escapeBuilder()
                    .nomsId(nomsId)
                    .provisionalCategorisation(RiskProfile.DEFAULT_CAT)
                    .onEscapeList(false)
                    .activeOnEscapeList(false)
                    .build();
        } else {
            log.debug("Alerts returned for  {} \n {} ", nomsId, escapeData.get());
            return EscapeProfile.escapeBuilder()
                    .nomsId(nomsId)
                    .provisionalCategorisation(RiskProfile.DEFAULT_CAT)
                    .onEscapeList(true)
                    .activeOnEscapeList(false).build();
        }

        /*
        List<Alert> alerts = escapeData.get();

        alerts.stream().map(alert -> {
            alert.setRanking(getRanking(alert));
            return alert;
        });

        final Alert alert = alerts.stream().max(Comparator.comparing(Alert::getRanking)).get();
        log.debug("Highest ranked alert {}", alert);

        var escape = EscapeProfile.escapeBuilder()
                .nomsId(nomsId)
                .provisionalCategorisation(mapCategorisation(alert.getRanking()))
                .onEscapeList(true)
                .activeOnEscapeList(alert.isActive());
        // etc

        return escape.build();
        */

    }

    /*
    private String mapCategorisation(int ranking) {

    }

    private int getRanking(Alert a){
        LocalDate oneYearAgo  = LocalDate.now().minusYears(1);
        LocalDate sixMonthsAgo  = LocalDate.now().minusMonths(6);

        if(a.getAlertCode().equals("XEL")){
            if(a.isActive()){
                return 12;
            }
            else {
                if(a.getDateExpires().isAfter(oneYearAgo)){
                    return 10;

                }else{
                    return 4;
                }
            }
        }
        else {
            if(a.isActive()){
                return 8;
            }
            else {
                if(a.getDateExpires().isAfter(sixMonthsAgo)){
                    return 6;

                }else{
                    return 2;
                }
            }
        }
    }

    private int getCategorisation(Alert a){
        if(a.getRanking())
    }
    */
}
