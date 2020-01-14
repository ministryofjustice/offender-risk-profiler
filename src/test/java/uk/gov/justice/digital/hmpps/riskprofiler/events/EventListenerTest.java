package uk.gov.justice.digital.hmpps.riskprofiler.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService;
import uk.gov.justice.digital.hmpps.riskprofiler.services.PollPrisonersService;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventListenerTest {

    private static final String OFFENDER_1 = "AB1234A";
    private static final String OFFENDER_2 = "AB1234B";
    private static final Long BOOKING_1 = 123456L;
    private static final Long INCIDENT_1 = 456123L;

    private EventListener service;

    @Mock
    private NomisService nomisService;
    @Mock
    private PollPrisonersService pollPrisonersService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        service = new EventListener(nomisService, pollPrisonersService, objectMapper);
    }

    @Test
    public void testAlertsEscape() {

        when(nomisService.getOffender(BOOKING_1)).thenReturn(OFFENDER_1);

        service.eventListener("{ \"Message\":\"{ \\\"eventType\\\":\\\"ALERT-INSERTED\\\", \\\"alertCode\\\":\\\"XER\\\", \\\"bookingId\\\":" + BOOKING_1 + " }\"}");

        verify(nomisService).evictEscapeListAlertsCache(OFFENDER_1);
        verify(nomisService, never()).evictSocListAlertsCache(OFFENDER_1);
        verify(pollPrisonersService).pollPrisoner(OFFENDER_1);
        verify(nomisService, never()).getPartiesOfIncident(any());
    }

    @Test
    public void testAlertsSoc() {

        when(nomisService.getOffender(BOOKING_1)).thenReturn(OFFENDER_1);

        service.eventListener("{ \"Message\":\"{ \\\"eventType\\\":\\\"ALERT-UPDATED\\\", \\\"alertCode\\\":\\\"XEAN\\\", \\\"bookingId\\\":" + BOOKING_1 + " }\"}");

        verify(nomisService, never()).evictEscapeListAlertsCache(OFFENDER_1);
        verify(nomisService).evictSocListAlertsCache(OFFENDER_1);
        verify(pollPrisonersService).pollPrisoner(OFFENDER_1);
        verify(nomisService, never()).getPartiesOfIncident(any());
    }

    @Test
    public void testAlertsIrrelevant() {

        service.eventListener("{ \"Message\":\"{ \\\"eventType\\\":\\\"ALERT-INSERTED\\\", \\\"alertCode\\\":\\\"OTHER\\\", \\\"bookingId\\\":" + BOOKING_1 + " }\"}");

        verify(nomisService, never()).evictEscapeListAlertsCache(any());
        verify(nomisService, never()).evictSocListAlertsCache(any());
        verify(pollPrisonersService, never()).pollPrisoner(any());
        verify(nomisService, never()).getPartiesOfIncident(any());
    }

    @Test
    public void testIncidents() {

        when(nomisService.getPartiesOfIncident(INCIDENT_1)).thenReturn(Arrays.asList(OFFENDER_1, OFFENDER_2));

        service.eventListener("{ \"Message\": \"{ \\\"eventType\\\":\\\"INCIDENT-CHANGED-CASES\\\", \\\"incidentCaseId\\\":" + INCIDENT_1 + " }\"}");

        verify(nomisService).evictIncidentsCache(OFFENDER_1);
        verify(nomisService).evictIncidentsCache(OFFENDER_2);
        verify(nomisService, never()).getOffender(any());
    }

    @Test
    public void testInvalidMessage() {

        service.eventListener("text contents");
        verify(nomisService, never()).getOffender(any());
        verify(nomisService, never()).getPartiesOfIncident(any());
    }
}
