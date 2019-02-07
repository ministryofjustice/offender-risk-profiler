package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.mockito.Mockito.when;

@Ignore
public class EscapeDecisionTreeServiceTest {

    public static final String OFFENDER_1 = "AB1234A";
    final LocalDate pretendNow = LocalDate.of(2018, Month.JULY, 26);
    final LocalDate expired8Months = LocalDate.of(2017, Month.NOVEMBER, 26);
    final LocalDate expired4Months = LocalDate.of(2017, Month.MARCH, 26);
    final LocalDate expired14Months = LocalDate.of(2017, Month.MAY, 26);
    final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private EscapeDecisionTreeService service;

    @MockBean
    private NomisService nomisService;


    @Before
    public void setup() {

    }
    @Test
    public void testHeightendResponse() {
        Alert xel12 = Alert.builder().active(true).alertCode("XEL").build();
        Alert xel = Alert.builder().active(false).alertCode("XEL").dateExpires(expired8Months.format(dateFormatter)).build();

        when(nomisService.getEscapeList(OFFENDER_1)).thenReturn(Optional.of(ImmutableList.of(xel12, xel)));

        final EscapeProfile escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile).extracting("provisionalCategorisation").isEqualTo("B");
    }

    @Test
    public void testHeightendInactiveResponse() {
        Alert xel = Alert.builder().active(false).alertCode("XEL").dateExpires(expired8Months.format(dateFormatter)).build();

        when(nomisService.getEscapeList(OFFENDER_1)).thenReturn(Optional.of(ImmutableList.of(xel)));

        final EscapeProfile escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile).extracting("provisionalCategorisation").isEqualTo("B");
    }

    @Test
    public void testStandardResponse() {
        Alert xel = Alert.builder().active(true).alertCode("XER").build();

        when(nomisService.getEscapeList(OFFENDER_1)).thenReturn(Optional.of(ImmutableList.of(xel)));

        final EscapeProfile escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile).extracting("provisionalCategorisation").isEqualTo("B");
    }

    @Test
    public void testStandardInactiveResponse() {
        Alert xel = Alert.builder().active(false).alertCode("XER").dateExpires(expired4Months.format(dateFormatter)).build();

        when(nomisService.getEscapeList(OFFENDER_1)).thenReturn(Optional.of(ImmutableList.of(xel)));

        final EscapeProfile escapeProfile = service.getEscapeProfile(OFFENDER_1);
        Assertions.assertThat(escapeProfile).extracting("provisionalCategorisation").isEqualTo("C");
    }

}