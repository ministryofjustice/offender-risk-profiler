package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriTemplate;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PagingAndSortingDto;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private static final String[] ESCAPE_LIST_ALERT_TYPES = {"XER", "XEL"};

    private static final String[] SOC_ALERT_TYPES = {"PL3", "PVN", "HPI", "XCO", "XD", "XEAN", "XEBM",
            "XFO", "XGANG", "XOCGN", "XP", "XSC"};

    private final RestCallHelper restCallHelper;

    public NomisService(final RestCallHelper restCallHelper) {
        this.restCallHelper = restCallHelper;
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

    List<Alert> getAlertsForOffender(final String nomsId, final String... alertTypes) {
        log.info("Getting alerts for noms id {} and types {}", nomsId, alertTypes);

        final var types = Arrays.stream(alertTypes)
                .map(alertType -> format("alertCode:eq:'%s'", alertType))
                .collect(Collectors.joining(",or:"));

        final var uriAlertsForOffenderByType = "/offenders/{nomsId}/alerts?query={types}&latestOnly=false";
        final var uri = new UriTemplate(uriAlertsForOffenderByType).expand(nomsId, types);
        return restCallHelper.getForList(uri, ALERTS).getBody();
    }

    public List<String> getAlertCandidates(@NotNull final LocalDateTime fromDateTime) {
        log.info("Getting alert candidates");

        final var uri = new UriTemplate("/offenders/alerts/candidates?fromDateTime={fromDateTime}").expand(fromDateTime);
        return getCandidates(uri);
    }

    @Cacheable("incident")
    public List<IncidentCase> getIncidents(@NotNull final String nomsId, @NotNull final List<String> incidentTypes, final List<String> participationRoles) {
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

    public List<String> getIncidentCandidates(@NotNull final LocalDateTime fromDateTime) {
        log.info("Getting incident candidates");

        final var uri = new UriTemplate("/offenders/incidents/candidates?fromDateTime={fromDateTime}").expand(fromDateTime);
        return getCandidates(uri);
    }

    List<String> getCandidates(URI uri) {
        final var results = restCallHelper.getWithPaging(uri,
                new PagingAndSortingDto(0L, 1000L), new ParameterizedTypeReference<List<String>>() {
                });
        final var body = results.getBody();
        final var total = getLongHeader(results, PagingAndSortingDto.HEADER_TOTAL_RECORDS);
        final var limit = getLongHeader(results, PagingAndSortingDto.HEADER_PAGE_LIMIT);
        if (total != null && limit != null && total > limit) {
            final var fullList = new ArrayList<>(body);
            fullList.addAll(restCallHelper.getWithPaging(uri,
                    new PagingAndSortingDto(limit, total), new ParameterizedTypeReference<List<String>>() {
                    }).getBody());
            return fullList;
        }
        return body;
    }

    private static Long getLongHeader(ResponseEntity<List<String>> results, String header) {
        if (results.getHeaders() != null && !CollectionUtils.isEmpty(results.getHeaders().get(header))) {
            return Long.parseLong(results.getHeaders().get(header).get(0));
        }
        return null;
    }

    public List<String> getOffendersAtPrison(@NotNull final String prisonId) {
        final var uri = new UriTemplate(format("/bookings?query=agencyId:eq:'%s'", prisonId)).expand();

        final var results = restCallHelper.getWithPaging(uri, new PagingAndSortingDto(0L, (long) Integer.MAX_VALUE), OFFENDERS).getBody();
        return results.stream().map(m -> (String) m.get("offenderNo")).collect(Collectors.toList());
    }
}
