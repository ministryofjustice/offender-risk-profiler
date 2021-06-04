package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.ViperRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Viper;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase;
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ViolenceDecisionTreeServiceTest {

    private static final String OFFENDER_1 = "AB1234A";
    private ViolenceDecisionTreeService service;

    @Mock
    private NomisService nomisService;
    @Mock
    private ViperRepository viperRepo;

    @BeforeEach
    public void setup() {
        service = new ViolenceDecisionTreeService(viperRepo, nomisService, 2, 6, new BigDecimal("2.50"));
    }

    @Test
    public void testNotOnViperFile() {
        when(viperRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());
        final var socProfile = service.getViolenceProfile(OFFENDER_1);

        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.getNumberOfAssaults()).isEqualTo(0);
    }

    @Test
    public void testNotOnViperFileButSeriousAssault() {
        when(viperRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.empty());

        final var now = LocalDateTime.now();
        when(nomisService.getIncidents(OFFENDER_1)).thenReturn(
                Arrays.asList(
                        IncidentCase.builder().incidentStatus("CLOSE").reportTime(now.minusMonths(2))
                                .responses(Arrays.asList(
                                        IncidentResponse.builder().question("Question 1").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 2").answer("NO").build(),
                                        IncidentResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 4").answer("NO").build()
                                )).build()
                )
        );
        final var socProfile = service.getViolenceProfile(OFFENDER_1);

        Assertions.assertThat(socProfile.isDisplayAssaults()).isTrue();
        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.getNumberOfAssaults()).isEqualTo(1);
        Assertions.assertThat(socProfile.getNumberOfSeriousAssaults()).isEqualTo(1);
        Assertions.assertThat(socProfile.getNumberOfNonSeriousAssaults()).isEqualTo(0);
        Assertions.assertThat(socProfile.isNotifySafetyCustodyLead()).isFalse();
    }

    @Test
    public void testOnViperFileWithSeriousAssaults() {
        when(viperRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Viper.builder().nomisId(OFFENDER_1).score(new BigDecimal("2.51")).build()));

        final var now = LocalDateTime.now();
        when(nomisService.getIncidents(OFFENDER_1)).thenReturn(
                Arrays.asList(
                        IncidentCase.builder().incidentStatus("CLOSE").reportTime(now.minusMonths(2))
                                .responses(Arrays.asList(
                                        IncidentResponse.builder().question("Question 1").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 2").answer("NO").build(),
                                        IncidentResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 4").answer("NO").build()
                                )).build(),
                        IncidentCase.builder().incidentStatus("CLOSE").reportTime(now.minusMonths(5))
                                .responses(Arrays.asList(
                                        IncidentResponse.builder().question("Question 1").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 2").answer("NO").build(),
                                        IncidentResponse.builder().question("Question 3").answer("YES").build(),
                                        IncidentResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").answer("NO").build()
                                )).build(),
                        IncidentCase.builder().incidentStatus("CLOSE").reportTime(now.minusMonths(7))
                                .responses(Arrays.asList(
                                        IncidentResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 2").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 3").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 4").answer("NO").build()
                                )).build(),
                        IncidentCase.builder().incidentStatus("DUP").reportTime(now.minusMonths(8))
                                .responses(Arrays.asList(
                                        IncidentResponse.builder().question("Question 1").answer("NO").build(),
                                        IncidentResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").answer("NO").build(),
                                        IncidentResponse.builder().question("Question 3").answer("NO").build(),
                                        IncidentResponse.builder().question("Question 4").answer("NO").build()
                                )).build()

                )
        );
        final var socProfile = service.getViolenceProfile(OFFENDER_1);

        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("B");
        Assertions.assertThat(socProfile.getNumberOfAssaults()).isEqualTo(3);
        Assertions.assertThat(socProfile.getNumberOfSeriousAssaults()).isEqualTo(1);
        Assertions.assertThat(socProfile.getNumberOfNonSeriousAssaults()).isEqualTo(1);
    }

    @Test
    public void testOnViperFileWithOldSeriousAssaults() {
        when(viperRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Viper.builder().nomisId(OFFENDER_1).score(new BigDecimal("2.51")).build()));

        final var now = LocalDateTime.now();
        when(nomisService.getIncidents(OFFENDER_1)).thenReturn(
                Arrays.asList(
                        IncidentCase.builder().incidentStatus("CLOSE").reportTime(now.minusMonths(7))
                                .responses(Arrays.asList(
                                        IncidentResponse.builder().question("Question 1").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 2").answer("NO").build(),
                                        IncidentResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 4").answer("NO").build()
                                )).build(),
                        IncidentCase.builder().incidentStatus("CLOSE").reportTime(now.minusMonths(5))
                                .responses(Arrays.asList(
                                        IncidentResponse.builder().question("Question 1").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 2").answer("NO").build(),
                                        IncidentResponse.builder().question("Question 3").answer("YES").build(),
                                        IncidentResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").answer("NO").build()
                                )).build()
                )
        );
        final var socProfile = service.getViolenceProfile(OFFENDER_1);

        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.getNumberOfAssaults()).isEqualTo(2);
        Assertions.assertThat(socProfile.getNumberOfSeriousAssaults()).isEqualTo(0);
        Assertions.assertThat(socProfile.getNumberOfNonSeriousAssaults()).isEqualTo(1);
    }

    @Test
    public void testOnViperFileWithBelowTriggerForAssaults() {
        when(viperRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Viper.builder().nomisId(OFFENDER_1).score(new BigDecimal("2.51")).build()));

        final var now = LocalDateTime.now();
        when(nomisService.getIncidents(OFFENDER_1)).thenReturn(
                Arrays.asList(
                        IncidentCase.builder().incidentStatus("CLOSE").reportTime(now.minusMonths(3))
                                .responses(Arrays.asList(
                                        IncidentResponse.builder().question("Question 1").answer("YES").build(),
                                        IncidentResponse.builder().question("Question 2").answer("NO").build(),
                                        IncidentResponse.builder().question("WAS A SERIOUS INJURY SUSTAINED").answer("NO").build(),
                                        IncidentResponse.builder().question("Question 4").answer("NO").build()
                                )).build()
                )
        );
        final var socProfile = service.getViolenceProfile(OFFENDER_1);

        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.getNumberOfAssaults()).isEqualTo(1);
        Assertions.assertThat(socProfile.getNumberOfSeriousAssaults()).isEqualTo(0);
        Assertions.assertThat(socProfile.getNumberOfNonSeriousAssaults()).isEqualTo(1);
    }

    @Test
    public void testOnViperFileWitLowViperScore() {
        when(viperRepo.getByKey(eq(OFFENDER_1))).thenReturn(Optional.of(Viper.builder().nomisId(OFFENDER_1).score(new BigDecimal("2.49")).build()));

        final var socProfile = service.getViolenceProfile(OFFENDER_1);

        Assertions.assertThat(socProfile.getProvisionalCategorisation()).isEqualTo("C");
        Assertions.assertThat(socProfile.getNumberOfAssaults()).isEqualTo(0);
        Assertions.assertThat(socProfile.getNumberOfSeriousAssaults()).isEqualTo(0);
    }
}
