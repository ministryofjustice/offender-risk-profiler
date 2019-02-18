package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Slf4j
public class NomisService {
    private static final ParameterizedTypeReference<List<Alert>> ALERTS = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<IncidentCase>> INCIDENTS = new ParameterizedTypeReference<>() {};
    private static final String[] ESCAPE_LIST_ALERT_TYPES = {"XER", "XEL"};

    private static final String[] SOC_ALERT_TYPES = {"PL3", "PVN", "HPI", "XCO", "XD", "XEAN", "XEBM",
            "XFO", "XGANG", "XOCGN", "XP", "XSC"};

    private final RestCallHelper restCallHelper;

    public NomisService(RestCallHelper restCallHelper) {
        this.restCallHelper = restCallHelper;
    }

    public List<Alert> getEscapeListAlertsForOffender(String nomsId) {
        log.info("Getting escape list alerts for noms id {}", nomsId);
        return getAlertsForOffender(nomsId, ESCAPE_LIST_ALERT_TYPES);
    }

    public List<Alert> getSocListAlertsForOffender(String nomsId) {
        log.info("Getting soc list alerts for noms id {}", nomsId);
        return getAlertsForOffender(nomsId, SOC_ALERT_TYPES);
    }

    List<Alert> getAlertsForOffender(String nomsId, String ... alertTypes) {
        log.info("Getting alerts for noms id {} and types {}", nomsId, alertTypes);

        var types = Arrays.stream(alertTypes)
                .map(alertType -> format("alertCode:eq:'%s'", alertType))
                .collect(Collectors.joining(":or:"));

        var uriAlertsForOffenderByType = "/offenders/{nomsId}/alerts?query={types}";
        var uri = new UriTemplate(uriAlertsForOffenderByType).expand(nomsId, types);
        return restCallHelper.getForList(uri, ALERTS).getBody();
    }

    public List<IncidentCase> getIncidents(@NotNull String nomsId, @NotNull List<String> incidentTypes, List<String> participationRoles) {
        log.info("Getting incidents for noms id {} and type {}, with roles of {}", nomsId, incidentTypes, participationRoles);

        var incidentTypesStr = incidentTypes.stream()
                .map(incidentType -> format("incidentType=%s", incidentType))
                .collect(Collectors.joining("&"));

        var participationRolesStr = participationRoles.stream()
                .map(participationRole -> format("participationRoles=%s", participationRole))
                .collect(Collectors.joining("&"));

        var uriIncidentsForOffender = format("/offenders/%s/incidents?%s&%s", nomsId, incidentTypesStr, participationRolesStr);
        var uri = new UriTemplate(uriIncidentsForOffender).expand();
        return restCallHelper.getForList(uri, INCIDENTS).getBody();
    }
}
