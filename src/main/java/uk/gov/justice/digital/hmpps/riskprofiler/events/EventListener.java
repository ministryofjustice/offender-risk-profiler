package uk.gov.justice.digital.hmpps.riskprofiler.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderEvent;
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService;
import uk.gov.justice.digital.hmpps.riskprofiler.services.PollPrisonersService;

import java.io.IOException;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "sqs.provider")
@Slf4j
public class EventListener {

    private final NomisService nomisService;
    private final PollPrisonersService pollPrisonersService;
    private final ObjectMapper objectMapper;

    public EventListener(final NomisService nomisService, final PollPrisonersService pollPrisonersService, final ObjectMapper objectMapper) {
        this.nomisService = nomisService;
        this.pollPrisonersService = pollPrisonersService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${sqs.events.queue.name}")
    public void eventListener(final String requestJson) {
        final var event = getOffenderEvent(requestJson);
        if (event != null) {
            switch (event.getEventType()) {
                case "ALERT-INSERTED":
                case "ALERT-UPDATED":
                case "ALERT-DELETED":
                    final var isEscape = NomisService.ESCAPE_LIST_ALERT_TYPES.contains(event.getAlertCode());
                    final var isSoc = NomisService.SOC_ALERT_TYPES.contains(event.getAlertCode());
                    if ((isEscape || isSoc) && event.getBookingId() != null) {
                        // booking id can be null for ALERT-DELETED. In this case ignore, as it is unlikely it will trigger need to change to cat B
                        final var nomsId = nomisService.getOffender(event.getBookingId());
                        if (isEscape) {
                            nomisService.evictEscapeListAlertsCache(nomsId);
                        }
                        if (isSoc) {
                            nomisService.evictSocListAlertsCache(nomsId);
                        }
                        pollPrisonersService.pollPrisoner(nomsId);
                    }
                    break;
                case "INCIDENT-INSERTED":
                case "INCIDENT-CHANGED-CASES":
                case "INCIDENT-CHANGED-PARTIES":
                case "INCIDENT-CHANGED-RESPONSES":
                case "INCIDENT-CHANGED-REQUIREMENTS":
                    final var nomsIds = nomisService.getPartiesOfIncident(event.getIncidentCaseId());
                    // TODO: Do not poll immediately (leave for batch) as one incident generates a lot of events
                    // Also should apply filter choosing only the relevant types of Assault incidents
                    nomsIds.forEach(nomisService::evictIncidentsCache);
                    break;
            }
        }
    }

    private OffenderEvent getOffenderEvent(final String requestJson) {
        OffenderEvent event = null;
        try {
            final Map<String, String> message = objectMapper.readValue(requestJson, Map.class);
            if (message == null || message.get("Message") == null) {
                log.warn(requestJson);
            } else {
                log.debug(message.get("Message")); // do not log an excessive amount of data
                event = objectMapper.readValue(message.get("Message"), OffenderEvent.class);
            }
        } catch (final IOException e) {
            log.error("Failed to Parse Message {}", requestJson);
        }
        return event;
    }
}
