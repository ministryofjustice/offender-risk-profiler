package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.riskprofiler.dao.ViperRepository
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Viper
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentResponse
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ViolenceDecisionTreeServiceTest {
  private lateinit var service: ViolenceDecisionTreeService

  @Mock
  private lateinit var nomisService: NomisService

  @Mock
  private lateinit var viperRepo: ViperRepository

  @BeforeEach
  fun setup() {
    service = ViolenceDecisionTreeService(viperRepo, nomisService, 2, 6, BigDecimal("2.50"))
  }

  @Test
  fun testNotOnViperFile() {
    Mockito.`when`(viperRepo.getByKey(ArgumentMatchers.eq(OFFENDER_1))).thenReturn(Optional.empty())
    val (_, provisionalCategorisation, _, _, _, numberOfAssaults) = service.getViolenceProfile(OFFENDER_1)
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(numberOfAssaults).isEqualTo(0)
  }

  @Test
  fun testNotOnViperFileButSeriousAssault() {
    Mockito.`when`(viperRepo.getByKey(ArgumentMatchers.eq(OFFENDER_1))).thenReturn(Optional.empty())
    val now = LocalDateTime.now()
    Mockito.`when`(nomisService.getIncidents(OFFENDER_1)).thenReturn(
      listOf(
        IncidentCase(
          "CLOSE",
          now.minusMonths(2),
          listOf(
            IncidentResponse("Question 1", "YES"),
            IncidentResponse("Question 2", "NO"),
            IncidentResponse("WAS A SERIOUS INJURY SUSTAINED", "YES"),
            IncidentResponse("Question 4", "NO")
          )
        )
      )
    )
    val (_, provisionalCategorisation, _, notifySafetyCustodyLead, displayAssaults, numberOfAssaults, numberOfSeriousAssaults, numberOfNonSeriousAssaults) = service.getViolenceProfile(
      OFFENDER_1
    )
    Assertions.assertThat(displayAssaults).isTrue
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(numberOfAssaults).isEqualTo(1)
    Assertions.assertThat(numberOfSeriousAssaults).isEqualTo(1)
    Assertions.assertThat(numberOfNonSeriousAssaults).isEqualTo(0)
    Assertions.assertThat(notifySafetyCustodyLead).isFalse
  }

  @Test
  fun testOnViperFileWithSeriousAssaults() {
    val viper = Viper(OFFENDER_1)
    viper.score = BigDecimal("2.51")
    Mockito.`when`(viperRepo.getByKey(ArgumentMatchers.eq(OFFENDER_1))).thenReturn(Optional.of(viper))
    val now = LocalDateTime.now()
    Mockito.`when`(nomisService.getIncidents(OFFENDER_1)).thenReturn(
      listOf(
        IncidentCase(
          "CLOSE",
          now.minusMonths(2),
          listOf(
            IncidentResponse("Question 1", "YES"),
            IncidentResponse("Question 2", "NO"),
            IncidentResponse("WAS A SERIOUS INJURY SUSTAINED", "YES"),
            IncidentResponse("Question 4", "NO")
          )
        ),
        IncidentCase(
          "CLOSE",
          now.minusMonths(5),
          listOf(
            IncidentResponse("Question 1", "YES"),
            IncidentResponse("Question 2", "NO"),
            IncidentResponse("Question 3", "YES"),
            IncidentResponse("WAS A SERIOUS INJURY SUSTAINED", "NO")
          )
        ),
        IncidentCase(
          "CLOSE",
          now.minusMonths(7),
          listOf(
            IncidentResponse("WAS A SERIOUS INJURY SUSTAINED", "YES"),
            IncidentResponse("Question 2", "YES"),
            IncidentResponse("Question 3", "YES"),
            IncidentResponse("Question 4", "NO")
          )
        ),
        IncidentCase(
          "DUP",
          now.minusMonths(8),
          listOf(
            IncidentResponse("Question 1", "NO"),
            IncidentResponse("WAS A SERIOUS INJURY SUSTAINED", "NO"),
            IncidentResponse("Question 3", "NO"),
            IncidentResponse("Question 4", "NO")
          )
        )
      )
    )
    val (_, provisionalCategorisation, _, _, _, numberOfAssaults, numberOfSeriousAssaults, numberOfNonSeriousAssaults) = service.getViolenceProfile(
      OFFENDER_1
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("B")
    Assertions.assertThat(numberOfAssaults).isEqualTo(3)
    Assertions.assertThat(numberOfSeriousAssaults).isEqualTo(1)
    Assertions.assertThat(numberOfNonSeriousAssaults).isEqualTo(1)
  }

  @Test
  fun testOnViperFileWithOldSeriousAssaults() {
    val viper = Viper(OFFENDER_1)
    viper.score = BigDecimal("2.51")
    Mockito.`when`(viperRepo.getByKey(ArgumentMatchers.eq(OFFENDER_1))).thenReturn(Optional.of(viper))
    val now = LocalDateTime.now()
    Mockito.`when`(nomisService.getIncidents(OFFENDER_1)).thenReturn(
      listOf(
        IncidentCase(
          "CLOSE",
          now.minusMonths(7),
          listOf(
            IncidentResponse("Question 1", "YES"),
            IncidentResponse("Question 2", "NO"),
            IncidentResponse("WAS A SERIOUS INJURY SUSTAINED", "YES"),
            IncidentResponse("Question 4", "NO")
          )
        ),
        IncidentCase(
          "CLOSE",
          now.minusMonths(5),
          listOf(
            IncidentResponse("Question 1", "YES"),
            IncidentResponse("Question 2", "NO"),
            IncidentResponse("Question 3", "YES"),
            IncidentResponse("WAS A SERIOUS INJURY SUSTAINED", "NO")
          )
        )
      )
    )
    val (_, provisionalCategorisation, _, _, _, numberOfAssaults, numberOfSeriousAssaults, numberOfNonSeriousAssaults) = service.getViolenceProfile(
      OFFENDER_1
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(numberOfAssaults).isEqualTo(2)
    Assertions.assertThat(numberOfSeriousAssaults).isEqualTo(0)
    Assertions.assertThat(numberOfNonSeriousAssaults).isEqualTo(1)
  }

  @Test
  fun testOnViperFileWithBelowTriggerForAssaults() {
    val viper = Viper(OFFENDER_1)
    viper.score = BigDecimal("2.51")
    Mockito.`when`(viperRepo.getByKey(ArgumentMatchers.eq(OFFENDER_1))).thenReturn(Optional.of(viper))
    val now = LocalDateTime.now()
    Mockito.`when`(nomisService.getIncidents(OFFENDER_1)).thenReturn(
      listOf(
        IncidentCase(
          "CLOSE",
          now.minusMonths(3),
          listOf(
            IncidentResponse("Question 1", "YES"),
            IncidentResponse("Question 2", "NO"),
            IncidentResponse("WAS A SERIOUS INJURY SUSTAINED", "NO"),
            IncidentResponse("Question 4", "NO")
          )
        )
      )
    )
    val (_, provisionalCategorisation, _, _, _, numberOfAssaults, numberOfSeriousAssaults, numberOfNonSeriousAssaults) = service.getViolenceProfile(
      OFFENDER_1
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(numberOfAssaults).isEqualTo(1)
    Assertions.assertThat(numberOfSeriousAssaults).isEqualTo(0)
    Assertions.assertThat(numberOfNonSeriousAssaults).isEqualTo(1)
  }

  @Test
  fun testOnViperFileWitLowViperScore() {
    val viper = Viper(OFFENDER_1)
    viper.score = BigDecimal("2.49")
    Mockito.`when`(viperRepo.getByKey(ArgumentMatchers.eq(OFFENDER_1))).thenReturn(Optional.of(viper))
    val (_, provisionalCategorisation, _, _, _, numberOfAssaults, numberOfSeriousAssaults) = service.getViolenceProfile(
      OFFENDER_1
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(numberOfAssaults).isEqualTo(0)
    Assertions.assertThat(numberOfSeriousAssaults).isEqualTo(0)
  }

  companion object {
    private const val OFFENDER_1 = "AB1234A"
  }
}
