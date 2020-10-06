package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExtremismDecisionTreeServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    private ExtremismDecisionTreeService service;

    @Mock
    private PathfinderService pathfinderRepo;

    @BeforeEach
    public void setup() {
        service = new ExtremismDecisionTreeService(pathfinderRepo);
    }

    @Test
    public void testWhenPathfinderOnFileWithBand1NoPreviousOffences() {
        final PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding(1)
                .build();
        when(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        final var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isTrue();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isTrue();
    }

    @Test
    public void testWhenPathfinderOnFileWithBand2WithPreviousOffences() {
        final PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding(2)
                .build();
        when(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        final var extremismProfile = service.getExtremismProfile(OFFENDER_1, true);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("B");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isTrue();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isTrue();
    }

    @Test
    public void testWhenPathfinderOnFileWithBand3() {
        final PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding(3)
                .build();
        when(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        final var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isTrue();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isTrue();
    }

    @Test
    public void testWhenPathfinderOnFileWithBand4() {
        final PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding(4)
                .build();
        when(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        final var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isTrue();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isFalse();
    }

    @Test
    public void testWhenPathfinderOnFileWithNoBand() {
        final PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding(null)
                .build();
        when(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        final var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isTrue();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isFalse();
    }

    @Test
    public void testWhenPathfinderNotOnFile() {
        when(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(Optional.empty());
        final var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isFalse();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isFalse();
    }
}
