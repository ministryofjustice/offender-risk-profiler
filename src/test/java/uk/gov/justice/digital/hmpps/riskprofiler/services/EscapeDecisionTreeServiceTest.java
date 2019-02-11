package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class EscapeDecisionTreeServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    final LocalDate expired8Months = LocalDate.of(2017, Month.NOVEMBER, 26);
    final LocalDate expired4Months = LocalDate.of(2017, Month.MARCH, 26);

    private EscapeDecisionTreeService service;

    @Mock
    private NomisService nomisService;

    @Before
    public void setup() {
        service = new EscapeDecisionTreeService(nomisService);
    }

    @Test
    public void testHeightendResponse() {
        var xel12 = Alert.builder().active(true).alertCode("XEL").build();
        var xel = Alert.builder().active(false).alertCode("XEL").dateExpires(expired8Months).build();

        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xel12, xel));

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile).extracting("provisionalCategorisation").isEqualTo("B");
    }

    @Test
    public void testHeightendInactiveResponse() {
        var xel = Alert.builder().active(false).alertCode("XEL").dateExpires(expired8Months).build();

        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xel));

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile).extracting("provisionalCategorisation").isEqualTo("B");
    }

    @Test
    public void testStandardResponse() {
        var xel = Alert.builder().active(true).alertCode("XER").build();

        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xel));

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile).extracting("provisionalCategorisation").isEqualTo("B");
    }

    @Test
    public void testStandardInactiveResponse() {
        var xel = Alert.builder().active(false).alertCode("XER").dateExpires(expired4Months).build();

        when(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xel));

        var escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile).extracting("provisionalCategorisation").isEqualTo("C");
    }

}