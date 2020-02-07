package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderBooking;
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderSentenceTerms;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LifeDecisionTreeServiceTest {

    private static final String OFFENDER_1 = "A1234AB";
    private static final Long BOOKING_1 = -1L;
    private LifeDecisionTreeService service;

    private final OffenderSentenceTerms lifeSentence = OffenderSentenceTerms.builder()
            .bookingId(BOOKING_1)
            .lifeSentence(true)
            .build();
    private final OffenderSentenceTerms nonLifeSentence = OffenderSentenceTerms.builder()
            .bookingId(BOOKING_1)
            .lifeSentence(false)
            .build();
    private final OffenderBooking lifeCode = OffenderBooking.builder()
            .bookingId(BOOKING_1)
            .imprisonmentStatus("CFLIFE")
            .build();
    private final OffenderBooking nonLifeCode = OffenderBooking.builder()
            .bookingId(BOOKING_1)
            .imprisonmentStatus("OTHER")
            .build();

    @Mock
    private NomisService nomisService;

    @Before
    public void setup() {
        service = new LifeDecisionTreeService(nomisService);
    }

    @Test
    public void testWhenLifeFlagTrue() {
        when(nomisService.getBooking(OFFENDER_1)).thenReturn(BOOKING_1);
        when(nomisService.getSentencesForOffender(BOOKING_1)).thenReturn(List.of(nonLifeSentence, lifeSentence));

        final var profile = service.getLifeProfile(OFFENDER_1);

        Assertions.assertThat(profile.getProvisionalCategorisation()).isEqualTo("B");
        Assertions.assertThat(profile.getNomsId()).isEqualTo(OFFENDER_1);
        Assertions.assertThat(profile.isLife()).isTrue();
    }

    @Test
    public void testWhenLifeCode() {
        when(nomisService.getBooking(OFFENDER_1)).thenReturn(BOOKING_1);
        when(nomisService.getSentencesForOffender(BOOKING_1)).thenReturn(List.of(nonLifeSentence));
        when(nomisService.getBookingDetails(BOOKING_1)).thenReturn(List.of(nonLifeCode, lifeCode));

        final var profile = service.getLifeProfile(OFFENDER_1);

        Assertions.assertThat(profile.getProvisionalCategorisation()).isEqualTo("B");
        Assertions.assertThat(profile.getNomsId()).isEqualTo(OFFENDER_1);
        Assertions.assertThat(profile.isLife()).isTrue();
    }

    @Test
    public void testWhenMurder() {
        when(nomisService.getBooking(OFFENDER_1)).thenReturn(BOOKING_1);
        when(nomisService.getSentencesForOffender(BOOKING_1)).thenReturn(List.of(nonLifeSentence));
        when(nomisService.getBookingDetails(BOOKING_1)).thenReturn(List.of(nonLifeCode));
        when(nomisService.getMainOffences(BOOKING_1)).thenReturn(List.of("Murder etc."));

        final var profile = service.getLifeProfile(OFFENDER_1);

        Assertions.assertThat(profile.getProvisionalCategorisation()).isEqualTo("B");
        Assertions.assertThat(profile.getNomsId()).isEqualTo(OFFENDER_1);
        Assertions.assertThat(profile.isLife()).isTrue();
    }

    @Test
    public void testWhenNotLife() {
        when(nomisService.getBooking(OFFENDER_1)).thenReturn(BOOKING_1);
        when(nomisService.getSentencesForOffender(BOOKING_1)).thenReturn(List.of(nonLifeSentence));
        when(nomisService.getBookingDetails(BOOKING_1)).thenReturn(List.of(nonLifeCode, nonLifeCode));
        when(nomisService.getMainOffences(BOOKING_1)).thenReturn(List.of("Trivial etc.", "another"));

        final var profile = service.getLifeProfile(OFFENDER_1);

        Assertions.assertThat(profile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(profile.getNomsId()).isEqualTo(OFFENDER_1);
        Assertions.assertThat(profile.isLife()).isFalse();
    }
}
