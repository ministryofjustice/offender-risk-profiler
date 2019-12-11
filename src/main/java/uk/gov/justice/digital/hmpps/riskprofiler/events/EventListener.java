package uk.gov.justice.digital.hmpps.riskprofiler.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderAlertEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "sqs.provider")
@Slf4j
public class EventListener {

    private final ObjectMapper objectMapper;
    // private final ReconciliationService reconciliationService;

    public EventListener(/*final ReconciliationService reconciliationService, */final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // this.reconciliationService = reconciliationService;
    }

    @JmsListener(destination = "${sqs.events.queue.name}")
    public void eventListener(final String requestJson) {
        final var event = getOffenderEvent(requestJson);
        if (event != null) {
            log.debug("Received event {}", event);
            if (Arrays.asList("ALERT_CREATED","ALERT_UPDATED","ALERT_DELETED").contains(event.getEventType())) {
                // reconciliationService.checkMovementAndDeallocate(event);
            }
        }
    }

    private OffenderAlertEvent getOffenderEvent(final String requestJson) {
        OffenderAlertEvent event = null;
        try {
            final Map<String, String> message = objectMapper.readValue(requestJson, Map.class);
            if (message != null && message.get("Message") != null) {
                event = objectMapper.readValue(message.get("Message"), OffenderAlertEvent.class);
            }
        } catch (IOException e) {
            log.error("Failed to Parse Message {}", requestJson);
        }
        return event;
    }
}
