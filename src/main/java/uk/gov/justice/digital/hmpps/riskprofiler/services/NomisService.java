package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriTemplate;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.BookingDetails;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PagingAndSortingDto;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Slf4j
public class NomisService {
    private static final ParameterizedTypeReference<List<Alert>> ALERTS = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<IncidentCase>> INCIDENTS = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<Map>> OFFENDERS = new ParameterizedTypeReference<>() {
    };
    public static final List<String> ESCAPE_LIST_ALERT_TYPES = List.of("XER", "XEL");

    public static final List<String> SOC_ALERT_TYPES = List.of("PL3", "PVN", "HPI", "XCO", "XD", "XEAN", "XEBM",
            "XFO", "XGANG", "XOCGN", "XP", "XSC");

    private final RestCallHelper restCallHelper;
    private final List<String> incidentTypes;
    private final List<String> participationRoles;

    public NomisService(final RestCallHelper restCallHelper,
                        @Value("${app.assaults.incident.types:ASSAULT}") final List<String> incidentTypes,
                        @Value("${app.assaults.participation.roles}") final List<String> participationRoles) {
        this.restCallHelper = restCallHelper;
        this.incidentTypes = incidentTypes;
        this.participationRoles = participationRoles;
    }

    @Cacheable("escapeAlert")
    public List<Alert> getEscapeListAlertsForOffender(final String nomsId) {
        log.info("Getting escape list alerts for noms id {}", nomsId);
        return getAlertsForOffender(nomsId, ESCAPE_LIST_ALERT_TYPES);
    }

    @CacheEvict("escapeAlert")
    public void evictEscapeListAlertsCache(final String nomsId) {
        log.info("Evicting {} from escapeAlert cache", nomsId);
    }

    @Cacheable("socAlert")
    public List<Alert> getSocListAlertsForOffender(final String nomsId) {
        log.info("Getting soc list alerts for noms id {}", nomsId);
        return getAlertsForOffender(nomsId, SOC_ALERT_TYPES);
    }

    @CacheEvict("socAlert")
    public void evictSocListAlertsCache(final String nomsId) {
        log.info("Evicting {} from socAlert cache", nomsId);
    }

    List<Alert> getAlertsForOffender(final String nomsId, final List<String> alertTypes) {
        log.info("Getting alerts for noms id {} and types {}", nomsId, alertTypes);

        final var types = alertTypes.stream()
                .map(alertType -> format("alertCode:eq:'%s'", alertType))
                .collect(Collectors.joining(",or:"));

        final var uriAlertsForOffenderByType = "/offenders/{nomsId}/alerts?query={types}&latestOnly=false";
        final var uri = new UriTemplate(uriAlertsForOffenderByType).expand(nomsId, types);
        return restCallHelper.getForList(uri, ALERTS).getBody();
    }

    @Cacheable("incident")
    public List<IncidentCase> getIncidents(@NotNull final String nomsId) {
        log.info("Getting incidents for noms id {} and type {}, with roles of {}", nomsId, incidentTypes, participationRoles);

        final var incidentTypesStr = incidentTypes.stream()
                .map(incidentType -> format("incidentType=%s", incidentType))
                .collect(Collectors.joining("&"));

        final var participationRolesStr = participationRoles.stream()
                .map(participationRole -> format("participationRoles=%s", participationRole))
                .collect(Collectors.joining("&"));

        final var uriIncidentsForOffender = format("/offenders/%s/incidents?%s&%s", nomsId, incidentTypesStr, participationRolesStr);
        final var uri = new UriTemplate(uriIncidentsForOffender).expand();
        return restCallHelper.getForList(uri, INCIDENTS).getBody();
    }

    @CacheEvict("incident")
    public void evictIncidentsCache(final String nomsId) {
        log.info("Evicting {} from incident cache", nomsId);
    }

    public List<String> getOffendersAtPrison(@NotNull final String prisonId) {
        final var uri = new UriTemplate(format("/bookings?query=agencyId:eq:'%s'", prisonId)).expand();

        final var results = restCallHelper.getWithPaging(uri, new PagingAndSortingDto(0L, (long) Integer.MAX_VALUE), OFFENDERS).getBody();
        return results.stream().map(m -> (String) m.get("offenderNo")).collect(Collectors.toList());
    }

    public String getOffender(@NotNull final Long bookingId) {
        final var uri = new UriTemplate(format("/bookings/%d?basicInfo=true", bookingId)).expand();
        final var result = restCallHelper.get(uri, BookingDetails.class);
        return result.getOffenderNo();
    }

    public List<String> getPartiesOfIncident(@NotNull final Long incidentId) {
        final var uri = new UriTemplate(format("/incidents/%d", incidentId)).expand();
        final IncidentCase incident;
        try {
            incident = restCallHelper.get(uri, IncidentCase.class);
        } catch (final HttpClientErrorException.NotFound nf) {
            // 404: incident not found, OR has no questions answered yet
            return Collections.emptyList();
        }
        if (incident.getParties() == null || !incidentTypes.contains(incident.getIncidentType())) {
            return Collections.emptyList();
        }
        return incident.getParties().stream()
                .map(party -> party.getBookingId() == null ? null : getOffender(party.getBookingId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
