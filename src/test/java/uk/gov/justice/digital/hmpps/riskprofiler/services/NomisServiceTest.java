package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.BookingDetails;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentParty;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NomisServiceTest {

    private NomisService service;

    @Mock
    private WebClientCallHelper webClientCallHelper;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(webClientCallHelper);
        service = new NomisService(webClientCallHelper, List.of("ASSAULTS"), List.of("ACTINV", "ASSIAL"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAlertCall() throws Exception {

        var body = List.of(
                Alert.builder().alertCode("SOC").build()
        );

        var response = new ResponseEntity<>(body, HttpStatus.OK);

        when(webClientCallHelper.getForList(eq("/api/offenders/A1234AA/alerts?query=alertCode:eq:'SOC'&latestOnly=false"), isA(ParameterizedTypeReference.class)))
                .thenReturn(response);

        var alertsForOffender = service.getAlertsForOffender("A1234AA", Arrays.asList("SOC"));

        assertThat(alertsForOffender).hasSize(1);

        verify(webClientCallHelper).getForList(eq("/api/offenders/A1234AA/alerts?query=alertCode:eq:'SOC'&latestOnly=false"), isA(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(webClientCallHelper);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEscapeListCall() throws Exception {

        var body = List.of(
                Alert.builder().alertCode("XER").build(),
                Alert.builder().alertCode("XEL").build()
        );

        var response = new ResponseEntity<>(body, HttpStatus.OK);

        when(webClientCallHelper.getForList(eq("/api/offenders/A1234AA/alerts?query=alertCode:eq:'XER',or:alertCode:eq:'XEL'&latestOnly=false"), isA(ParameterizedTypeReference.class)))
                .thenReturn(response);

        var alertsForOffender = service.getEscapeListAlertsForOffender("A1234AA");

        assertThat(alertsForOffender).hasSize(2);

        verify(webClientCallHelper).getForList(eq("/api/offenders/A1234AA/alerts?query=alertCode:eq:'XER',or:alertCode:eq:'XEL'&latestOnly=false"), isA(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(webClientCallHelper);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIncidentCall() throws Exception {

        var body = List.of(
                IncidentCase.builder().build(),
                IncidentCase.builder().build()
        );

        var response = new ResponseEntity<>(body, HttpStatus.OK);

        when(webClientCallHelper.getForList(eq("/api/offenders/A1234AA/incidents?incidentType=ASSAULTS&participationRoles=ACTINV&participationRoles=ASSIAL"),
                isA(ParameterizedTypeReference.class)))
                .thenReturn(response);

        var incidentsForOffender = service.getIncidents("A1234AA");

        assertThat(incidentsForOffender).hasSize(2);

        verify(webClientCallHelper).getForList(eq("/api/offenders/A1234AA/incidents?incidentType=ASSAULTS&participationRoles=ACTINV&participationRoles=ASSIAL"), isA(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(webClientCallHelper);
    }

    @Test
    public void testGetPartiesOfIncidentHappy() throws Exception {
        final var incidentParty1 = IncidentParty.builder()
                .bookingId(12345L)
                .build();
        final var incidentParty2 = IncidentParty.builder()
                .bookingId(12346L)
                .build();

        final var incidentCase = IncidentCase.builder()
                .incidentCaseId(123L)
                .incidentType("ASSAULTS")
                .parties(List.of(incidentParty1, incidentParty2))
                .build();

        when(webClientCallHelper.get("/api/incidents/123", IncidentCase.class)).thenReturn(incidentCase);

        final var bookingDetails1 = BookingDetails
                .builder()
                .bookingId(12345L)
                .offenderNo("OFFENDER1")
                .build();
        when(webClientCallHelper.get("/api/bookings/12345?basicInfo=true", BookingDetails.class)).thenReturn(bookingDetails1);
        final var bookingDetails2 = BookingDetails
                .builder()
                .bookingId(12346L)
                .offenderNo("OFFENDER2")
                .build();
        when(webClientCallHelper.get("/api/bookings/12346?basicInfo=true", BookingDetails.class)).thenReturn(bookingDetails2);

        final var partiesOfIncident = service.getPartiesOfIncident(123L);

        assertThat(partiesOfIncident).asList().containsExactly("OFFENDER1", "OFFENDER2");
    }

    @Test
    public void testGetPartiesOfIncidentIrrelevantType() throws Exception {
        final var incidentParty = IncidentParty.builder()
                .bookingId(12345L)
                .build();

        final var incidentCase = IncidentCase.builder()
                .incidentCaseId(123L)
                .incidentType("OTHER")
                .parties(List.of(incidentParty))
                .build();

        when(webClientCallHelper.get("/api/incidents/123", IncidentCase.class)).thenReturn(incidentCase);

        final var partiesOfIncident = service.getPartiesOfIncident(123L);

        assertThat(partiesOfIncident).asList().isEmpty();
    }

    @Test
    public void testGetPartiesOfIncidentNoBookingId() throws Exception {
        final var incidentParty = IncidentParty.builder().build();

        final var incidentCase = IncidentCase.builder()
                .incidentCaseId(123L)
                .incidentType("ASSAULTS")
                .parties(List.of(incidentParty))
                .build();

        when(webClientCallHelper.get("/api/incidents/123", IncidentCase.class)).thenReturn(incidentCase);

        final var partiesOfIncident = service.getPartiesOfIncident(123L);

        assertThat(partiesOfIncident).asList().isEmpty();
    }

    @Test
    public void testGetPartiesOfIncidentNoParties() throws Exception {
        final var incidentCase = IncidentCase.builder()
                .incidentCaseId(123L)
                .incidentType("ASSAULTS")
                .build();

        when(webClientCallHelper.get("/api/incidents/123", IncidentCase.class)).thenReturn(incidentCase);

        final var partiesOfIncident = service.getPartiesOfIncident(123L);

        assertThat(partiesOfIncident).asList().isEmpty();
    }

    @Test
    public void testGetPartiesOfIncident404() throws Exception {
        when(webClientCallHelper.get("/api/incidents/123", IncidentCase.class)).thenThrow(
                WebClientResponseException.create(HttpStatus.NOT_FOUND.value(), "test", null, null, null));

        final var partiesOfIncident = service.getPartiesOfIncident(123L);

        assertThat(partiesOfIncident).asList().isEmpty();
    }
}
