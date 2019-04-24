package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PathfinderRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExtremismDecisionTreeServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    private ExtremismDecisionTreeService service;

    @Mock
    private PathfinderRepository pathfinderRepo;

    @Before
    public void setup() {
        service = new ExtremismDecisionTreeService(pathfinderRepo);
    }

    @Test
    public void testWhenPathfinderOnFileWithBand1NoPreviousOffences() {
        PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding("BAND 1")
                .build();
        when(pathfinderRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isTrue();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isTrue();
    }

    @Test
    public void testWhenPathfinderOnFileWithBand2WithPreviousOffences() {
        PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding("BAND 2")
                .build();
        when(pathfinderRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        var extremismProfile = service.getExtremismProfile(OFFENDER_1, true);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("B");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isTrue();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isTrue();
    }


    @Test
    public void testWhenPathfinderOnFileWithBand3() {
        PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding("BAND 3")
                .build();
        when(pathfinderRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isFalse();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isTrue();
    }

    @Test
    public void testWhenPathfinderOnFileWithBand4() {
        PathFinder pathFinder = PathFinder.builder()
                .nomisId(OFFENDER_1)
                .pathFinderBanding("BAND 4")
                .build();
        when(pathfinderRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(pathFinder));
        var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isFalse();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isFalse();
    }


    @Test
    public void testWhenPathfinderNotOnFile() {
        when(pathfinderRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        var extremismProfile = service.getExtremismProfile(OFFENDER_1, false);

        Assertions.assertThat(extremismProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(extremismProfile.isIncreasedRiskOfExtremism()).isFalse();
        Assertions.assertThat(extremismProfile.isNotifyRegionalCTLead()).isFalse();
    }


}
