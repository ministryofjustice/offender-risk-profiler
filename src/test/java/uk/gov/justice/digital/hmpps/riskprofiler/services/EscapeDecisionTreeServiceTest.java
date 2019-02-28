package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EscapeDecisionTreeServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    private static final Alert activeListAlert= Alert.builder().active(true).alertCode("XEL").build();
    private static final Alert activeRiskAlert= Alert.builder().active(true).alertCode("XER").build();
    private static final Alert inactiveRiskAlert= Alert.builder().active(false).alertCode("XER").build();
    private static final Alert inactiveListAlert= Alert.builder().active(false).alertCode("XEL").build();

    private EscapeDecisionTreeService service;

    @Mock
    private NomisService nomisService;

    @Before
    public void setup() {
        service = new EscapeDecisionTreeService(nomisService);
    }

    @Test
    public void testMixedResponse() {

        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(activeListAlert, activeRiskAlert, inactiveListAlert, inactiveRiskAlert));

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile.getEscapeListAlerts()).hasSize(1);
        Assertions.assertThat(escapeProfile.getEscapeRiskAlerts()).hasSize(1);
        Assertions.assertThat(escapeProfile.isActiveEscapeList()).isTrue();
        Assertions.assertThat(escapeProfile.isActiveEscapeRisk()).isTrue();
    }

    @Test
    public void testListResponse() {

        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(activeListAlert, activeListAlert));

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile.getEscapeListAlerts()).hasSize(2);
        Assertions.assertThat(escapeProfile.getEscapeRiskAlerts()).hasSize(0);
        Assertions.assertThat(escapeProfile.isActiveEscapeList()).isTrue();
        Assertions.assertThat(escapeProfile.isActiveEscapeRisk()).isFalse();
    }

    @Test
    public void testRiskResponse() {

        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(activeRiskAlert, activeRiskAlert));

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile.getEscapeListAlerts()).hasSize(0);
        Assertions.assertThat(escapeProfile.getEscapeRiskAlerts()).hasSize(2);
        Assertions.assertThat(escapeProfile.isActiveEscapeList()).isFalse();
        Assertions.assertThat(escapeProfile.isActiveEscapeRisk()).isTrue();
    }

    @Test
    public void testNoAlertsResponse() {
        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of());

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile.isActiveEscapeList()).isFalse();
        Assertions.assertThat(escapeProfile.isActiveEscapeRisk()).isFalse();
        Assertions.assertThat(escapeProfile.getEscapeListAlerts()).hasSize(0);
        Assertions.assertThat(escapeProfile.getEscapeRiskAlerts()).hasSize(0);
    }

    @Test
    public void testInactiveOnlyAlertsResponse() {
        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(inactiveListAlert, inactiveRiskAlert));

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile.isActiveEscapeList()).isFalse();
        Assertions.assertThat(escapeProfile.isActiveEscapeRisk()).isFalse();
        Assertions.assertThat(escapeProfile.getEscapeListAlerts()).hasSize(0);
        Assertions.assertThat(escapeProfile.getEscapeRiskAlerts()).hasSize(0);
    }


}