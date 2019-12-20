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

    public EventListener(NomisService nomisService, final PollPrisonersService pollPrisonersService, final ObjectMapper objectMapper) {
        this.nomisService = nomisService;
        this.pollPrisonersService = pollPrisonersService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "${sqs.events.queue.name}")
    public void eventListener(final String requestJson) {
        log.info("In eventListener with event: " + requestJson);
        final var event = getOffenderEvent(requestJson);
        if (event != null) {
            switch (event.getEventType()) {
                case "ALERT-INSERTED":
                case "ALERT-UPDATED":
                case "ALERT-DELETED":
                    final var nomsId = nomisService.getOffender(event.getBookingId());
                    if (NomisService.ESCAPE_LIST_ALERT_TYPES.contains(event.getAlertCode())) {
                        nomisService.evictEscapeListAlertsCache(nomsId);
                    }
                    if (NomisService.SOC_ALERT_TYPES.contains(event.getAlertCode())) {
                        nomisService.evictSocListAlertsCache(nomsId);
                    }
                    pollPrisonersService.pollPrisoner(nomsId);
                    break;
                case "INCIDENT-INSERTED":
                case "INCIDENT-CHANGED-CASES":
                case "INCIDENT-CHANGED-PARTIES":
                case "INCIDENT-CHANGED-RESPONSES":
                case "INCIDENT-CHANGED-REQUIREMENTS":
                    final var nomsIds = nomisService.getPartiesOfIncident(event.getIncidentCaseId());
                    nomsIds.forEach(offenderNo -> {
                        nomisService.evictIncidentsCache(offenderNo);
                        // TODO: Do not poll immediately (leave for batch) as one incident generates a lot of events
                        // pollPrisonersService.pollPrisoner(offenderNo);
                    });
                    break;
            }
        }
    }

    private OffenderEvent getOffenderEvent(final String requestJson) {
        OffenderEvent event = null;
        try {
            final Map<String, String> message = objectMapper.readValue(requestJson, Map.class);
            if (message != null && message.get("Message") != null) {
                event = objectMapper.readValue(message.get("Message"), OffenderEvent.class);
            }
        } catch (IOException e) {
            log.error("Failed to Parse Message {}", requestJson);
        }
        return event;
    }
}
