package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.BookingDetails;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase;
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderBooking;
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderSentenceTerms;
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
    private static final ParameterizedTypeReference<List<Map>> MAP = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<OffenderBooking>> BOOKING_DETAILS = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<OffenderSentenceTerms>> SENTENCE_TERMS = new ParameterizedTypeReference<>() {
    };
    public static final List<String> ESCAPE_LIST_ALERT_TYPES = List.of("XER", "XEL");

    public static final List<String> SOC_ALERT_TYPES = List.of("PL3", "PVN", "HPI", "XCO", "XD", "XEAN", "XEBM",
            "XFO", "XGANG", "XOCGN", "XP", "XSC");

    private final WebClientCallHelper webClientCallHelper;
    private final List<String> incidentTypes;
    private final List<String> participationRoles;

    public NomisService(final WebClientCallHelper webClientCallHelper,
                        @Value("${app.assaults.incident.types:ASSAULT}") final List<String> incidentTypes,
                        @Value("${app.assaults.participation.roles}") final List<String> participationRoles) {
        this.webClientCallHelper = webClientCallHelper;
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

        final var uriAlertsForOffenderByType = format("/api/offenders/%1$s/alerts?query=%2$s&latestOnly=false",nomsId, types);
        return webClientCallHelper.getForList(uriAlertsForOffenderByType, ALERTS).getBody();
    }

    public List<OffenderSentenceTerms> getSentencesForOffender(final Long bookingId) {
        log.info("Getting sentences for bookingId {}", bookingId);
        final var uri = format("/api/offender-sentences/booking/%s/sentenceTerms", bookingId);
        return webClientCallHelper.getForList(uri, SENTENCE_TERMS).getBody();
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

        final var uriIncidentsForOffender = format("/api/offenders/%s/incidents?%s&%s", nomsId, incidentTypesStr, participationRolesStr);
        return webClientCallHelper.getForList(uriIncidentsForOffender, INCIDENTS).getBody();
    }

    @CacheEvict("incident")
    public void evictIncidentsCache(final String nomsId) {
        log.info("Evicting {} from incident cache", nomsId);
    }

    public List<String> getOffendersAtPrison(@NotNull final String prisonId) {
        final var uri = format("/api/bookings?query=agencyId:eq:'%s'", prisonId);
        final List<Map> results;
        try {
            results = webClientCallHelper.getWithPaging(uri, new PagingAndSortingDto(0L, (long) Integer.MAX_VALUE), MAP).getBody();
        } catch (final WebClientResponseException.NotFound e) {
            log.warn("Prison does not exist");
            return List.of();
        }
        return results.stream().map(m -> (String) m.get("offenderNo")).collect(Collectors.toList());
    }

    public List<OffenderBooking> getBookingDetails(final Long bookingId) {
        log.info("Getting details for bookingId {}", bookingId);
        final var uri = // new UriTemplate("/bookings?bookingId={bookingId}").expand(bookingId);
          String.format("/api/bookings?bookingId=%s", bookingId);
        return webClientCallHelper.getForList(uri, BOOKING_DETAILS).getBody();
    }

    public List<String> getMainOffences(final Long bookingId) {
        final var uri = format("/api/bookings/%d/mainOffence", bookingId);
        final var results = webClientCallHelper.getForList(uri, MAP).getBody();
        return results.stream().map(m -> (String) m.get("offenceDescription")).collect(Collectors.toList());
    }

    public String getOffender(@NotNull final Long bookingId) {
        final var uri = format("/api/bookings/%d?basicInfo=true", bookingId);
        final var result = webClientCallHelper.get(uri, BookingDetails.class);
        return result.getOffenderNo();
    }

    public Long getBooking(@NotNull final String nomsId) {
        final var uri = format("/api/bookings/offenderNo/%s", nomsId);
        final var result = webClientCallHelper.get(uri, BookingDetails.class);
        return result.getBookingId();
    }

    public List<String> getPartiesOfIncident(@NotNull final Long incidentId) {
        final var uri = format("/api/incidents/%d", incidentId);
        final IncidentCase incident;
        try {
            incident = webClientCallHelper.get(uri, IncidentCase.class);
        } catch (final WebClientResponseException.NotFound nf) {
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
