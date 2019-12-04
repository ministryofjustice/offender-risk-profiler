package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderEvent;

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

    @JmsListener(destination = "${sqs.queue.name}")
    public void eventListener(@Payload final OffenderEvent event) {
        if (event != null) {
            switch (event.getEventType()) {
                case "ALERT-INSERTED":
                case "ALERT-UPDATED":
                case "ALERT-DELETED":
                    final var nomsId = nomisService.getOffender(event.getBookingId());
                    nomisService.evictEscapeListAlertsCache(nomsId);
                    nomisService.evictSocListAlertsCache(nomsId);
                    pollPrisonersService.pollPrisoner(nomsId);
                    break;
                case "INCIDENT-INSERTED":
                case "INCIDENT-CHANGED":
                    final var nomsId = nomisService.getOffender(event.getBookingId());
                    // TODO
                    break;
            }
        }
    }
}
