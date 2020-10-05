package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.*;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.hmpps.riskprofiler.services.SocDecisionTreeService.PRINCIPAL_SUBJECT;

@RunWith(MockitoJUnitRunner.class)
public class SocDecisionTreeServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    private SocDecisionTreeService service;

    @Mock
    private NomisService nomisService;
    @Mock
    private PrasRepository prasRepo;
    @Mock
    private OcgRepository ocgRepo;
    @Mock
    private OcgmRepository ocgmRepo;
    @Mock
    private ViperRepository viperRepo;

    @Before
    public void setup() {
        final var factory = new DataRepositoryFactory(ocgmRepo, ocgRepo, prasRepo, viperRepo);
        service = new SocDecisionTreeService(factory, nomisService);
    }

    @Test
    public void testOnPrasFile() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Pras.builder().nomisId(OFFENDER_1).build()));
        final var socProfile = service.getSocData(OFFENDER_1);

        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }

    @Test
    public void testNotOnPrasFileAndBandNotInList() {
        final var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(LocalDate.now().minusMonths(11)).build();
        final var xd = Alert.builder().active(false).alertCode("XD").dateExpires(LocalDate.now().minusYears(2)).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(OcgmList.builder().nomisId(OFFENDER_1).ocgm(Ocgm.builder().nomisId(OFFENDER_1).ocgId("123").build()).build()));
        when(ocgRepo.getByKey(eq("123"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("5c").build()));
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndBandInList() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(OcgmList.builder().nomisId(OFFENDER_1).ocgm(Ocgm.builder().nomisId(OFFENDER_1).ocgId("1234").build()).build()));
        when(ocgRepo.getByKey(eq("1234"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("2a").build()));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }


    @Test
    public void testNotOnPrasFileAndBandInListAndPrincipleStanding() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(
                OcgmList.builder().nomisId(OFFENDER_1)
                        .ocgm(
                            Ocgm.builder()
                                    .nomisId(OFFENDER_1)
                                    .standingWithinOcg(PRINCIPAL_SUBJECT)
                                    .ocgId("12345")
                                    .build()
                    ).build()));
        when(ocgRepo.getByKey(eq("12345"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("2a").build()));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }

    @Test
    public void testNotOnPrasFileAndNotInBandInListAndPrincipleStanding() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(OcgmList.builder().nomisId(OFFENDER_1)
                .ocgm(Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg(PRINCIPAL_SUBJECT)
                .ocgId("123456").build()).build()));
        when(ocgRepo.getByKey(eq("123456"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("4a").build()));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }

    @Test
    public void testNotOnPrasFileAndBandNotInListWithOldAlerts() {
        final var now = LocalDate.now();
        final var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(now.minusMonths(13)).build();
        final var xd = Alert.builder().active(false).alertCode("XD").dateExpires(now.minusYears(2)).dateExpires(now.minusMonths(16)).expired(true).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(OcgmList.builder().nomisId(OFFENDER_1)
                .ocgm(Ocgm.builder().nomisId(OFFENDER_1).ocgId("123").build()).build()));
        when(ocgRepo.getByKey(eq("123"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("5c").build()));
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndNoOcgmWithActiveAlerts() {
        final var now = LocalDate.now();
        final var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(now.minusMonths(11)).build();
        final var xd = Alert.builder().active(false).alertCode("XD").dateExpires(now.minusYears(2)).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndNoOcgmWithOldAlerts() {
        final var now = LocalDate.now();
        final var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(now.minusMonths(13)).build();
        final var xd = Alert.builder().active(false).alertCode("XD").dateExpires(now.minusYears(2)).dateExpires(now.minusMonths(16)).expired(true).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndHasOcgmNotNoOcgWithOldAlerts() {
        final var now = LocalDate.now();
        final var xfo = Alert.builder().active(true).alertCode("XFO").dateCreated(now.minusMonths(13)).build();
        final var xd = Alert.builder().active(false).alertCode("XD").dateExpires(now.minusYears(2)).dateExpires(now.minusMonths(16)).expired(true).build();

        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(OcgmList.builder().nomisId(OFFENDER_1)
                .ocgm(Ocgm.builder().nomisId(OFFENDER_1).ocgId("123").build()).build()));
        when(ocgRepo.getByKey(eq("123"))).thenReturn(Optional.empty());
        when(nomisService.getSocListAlertsForOffender(OFFENDER_1)).thenReturn(List.of(xfo, xd));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndOneWithInBandInListAndAnotherEntryWithoutAndPrincipleStanding() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        final Ocgm ocgm1 = Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg("SomethingElse")
                .ocgId("123456")
                .build();
        final Ocgm ocgm2 = Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg("SomethingElse")
                .ocgId("1234567")
                .build();
        final OcgmList ocgmList = OcgmList.builder().nomisId(OFFENDER_1)
                .ocgms(List.of(ocgm1, ocgm2)).build();

        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(ocgmList));
        when(ocgRepo.getByKey(eq("123456"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("4a").build()));
        when(ocgRepo.getByKey(eq("1234567"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("1a").build()));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isFalse();
    }

    @Test
    public void testNotOnPrasFileAndMultipleOffendersForSameNomsIDOneWithBand1a() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        final Ocgm ocgm1 = Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg("SomethingElse")
                .ocgId("123456")
                .build();
        final Ocgm ocgm2 = Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg("SomethingElse")
                .ocgId("1234567")
                .build();
        final Ocgm ocgm3 = Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg(PRINCIPAL_SUBJECT)
                .ocgId("1234568")
                .build();
        final OcgmList ocgmList = OcgmList.builder().nomisId(OFFENDER_1)
                .ocgms(List.of(ocgm1, ocgm2, ocgm3)).build();

        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(ocgmList));
        when(ocgRepo.getByKey(eq("123456"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("4a").build()));
        when(ocgRepo.getByKey(eq("1234567"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("5a").build()));
        when(ocgRepo.getByKey(eq("1234568"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("1a").build()));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }

    @Test
    public void testNotOnPrasFileAndMultipleOffendersForSameNomsIDOneWithBand5aButPrincipal() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        final Ocgm ocgm1 = Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg("SomethingElse")
                .ocgId("123456")
                .build();
        final Ocgm ocgm2 = Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg(PRINCIPAL_SUBJECT)
                .ocgId("1234567")
                .build();
        final Ocgm ocgm3 = Ocgm.builder().nomisId(OFFENDER_1)
                .standingWithinOcg("SomethingElse")
                .ocgId("1234568")
                .build();
        final OcgmList ocgmList = OcgmList.builder().nomisId(OFFENDER_1)
                .ocgms(List.of(ocgm1, ocgm2, ocgm3)).build();

        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(ocgmList));
        when(ocgRepo.getByKey(eq("123456"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("4a").build()));
        when(ocgRepo.getByKey(eq("1234567"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("5a").build()));
        when(ocgRepo.getByKey(eq("1234568"))).thenReturn(Optional.of(Ocg.builder().ocgmBand("1a").build()));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }

    @Test
    public void testPrisonerInListButBandMissing() {
        when(prasRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        when(ocgmRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(
                OcgmList.builder().nomisId(OFFENDER_1)
                        .ocgm(
                                Ocgm.builder()
                                        .nomisId(OFFENDER_1)
                                        .standingWithinOcg(PRINCIPAL_SUBJECT)
                                        .ocgId("12345")
                                        .build()
                        ).build()));
        // return ocg with no band:
        when(ocgRepo.getByKey(eq("12345"))).thenReturn(Optional.of(Ocg.builder().build()));

        final var socProfile = service.getSocData(OFFENDER_1);
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.isTransferToSecurity()).isTrue();
    }
}
