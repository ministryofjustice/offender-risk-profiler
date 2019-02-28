package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class NomisServiceTest {

    private NomisService service;

    @Mock
    private RestCallHelper restCallHelper;

    @Before
    public void setup() {
        initMocks(restCallHelper);
        service = new NomisService(restCallHelper);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAlertCall() throws Exception {

        var body = List.of(
                Alert.builder().alertCode("SOC").build()
        );

        var response = new ResponseEntity<>(body, HttpStatus.OK);

        when(restCallHelper.getForList(eq(new URI("/bookings/offenderNo/A1234AA/alerts?query=alertCode:eq:'SOC'")), isA(ParameterizedTypeReference.class)))
                .thenReturn(response);

        var alertsForOffender = service.getAlertsForOffender("A1234AA", "SOC");

        assertThat(alertsForOffender).hasSize(1);

        verify(restCallHelper).getForList(eq(new URI("/bookings/offenderNo/A1234AA/alerts?query=alertCode:eq:'SOC'")), isA(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restCallHelper);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEscapeListCall() throws Exception {

        var body = List.of(
                Alert.builder().alertCode("XER").build(),
                Alert.builder().alertCode("XEL").build()
        );

        var response = new ResponseEntity<>(body, HttpStatus.OK);

        when(restCallHelper.getForList(eq(new URI("/bookings/offenderNo/A1234AA/alerts?query=alertCode:eq:'XER',or:alertCode:eq:'XEL'")), isA(ParameterizedTypeReference.class)))
                .thenReturn(response);

        var alertsForOffender = service.getEscapeListAlertsForOffender("A1234AA");

        assertThat(alertsForOffender).hasSize(2);

        verify(restCallHelper).getForList(eq(new URI("/bookings/offenderNo/A1234AA/alerts?query=alertCode:eq:'XER',or:alertCode:eq:'XEL'")), isA(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restCallHelper);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIncidentCall() throws Exception {

        var body = List.of(
                IncidentCase.builder().build(),
                IncidentCase.builder().build()
        );

        var response = new ResponseEntity<>(body, HttpStatus.OK);

        when(restCallHelper.getForList(eq(new URI("/offenders/A1234AA/incidents?incidentType=ASSAULTS&participationRoles=ACTINV&participationRoles=ASSIAL")),
                isA(ParameterizedTypeReference.class)))
                .thenReturn(response);

        var incidentsForOffender = service.getIncidents("A1234AA", List.of("ASSAULTS"), List.of("ACTINV", "ASSIAL"));

        assertThat(incidentsForOffender).hasSize(2);

        verify(restCallHelper).getForList(eq(new URI("/offenders/A1234AA/incidents?incidentType=ASSAULTS&participationRoles=ACTINV&participationRoles=ASSIAL")), isA(ParameterizedTypeReference.class));
        verifyNoMoreInteractions(restCallHelper);
    }
}
