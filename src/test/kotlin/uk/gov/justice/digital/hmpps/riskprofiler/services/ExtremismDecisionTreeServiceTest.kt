package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder

@ExtendWith(MockitoExtension::class)
class ExtremismDecisionTreeServiceTest {
  lateinit var service: ExtremismDecisionTreeService

  private val pathfinderRepo: PathfinderService = mock()

  @BeforeEach
  fun setup() {
    service = ExtremismDecisionTreeService(pathfinderRepo)
  }

  @Test
  fun testWhenPathfinderOnFileWithBand1NoPreviousOffences() {
    val pathFinder = PathFinder(OFFENDER_1, 1)
    whenever(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(pathFinder)

    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service.getExtremismProfile(
      OFFENDER_1,
      false,
    )

    assertThat(provisionalCategorisation).isEqualTo("C")
    assertThat(increasedRiskOfExtremism).isTrue
    assertThat(notifyRegionalCTLead).isTrue
  }

  @Test
  fun testWhenPathfinderOnFileWithBand2WithPreviousOffences() {
    val pathFinder = PathFinder(OFFENDER_1, 2)
    whenever(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(pathFinder)

    val (
      _,
      provisionalCategorisation,
      notifyRegionalCTLead,
      increasedRiskOfExtremism,
    ) = service.getExtremismProfile(
      OFFENDER_1,
      true,
    )

    assertThat(provisionalCategorisation).isEqualTo("B")
    assertThat(increasedRiskOfExtremism).isTrue
    assertThat(notifyRegionalCTLead).isTrue
  }

  @Test
  fun testWhenPathfinderOnFileWithBand3() {
    val pathFinder = PathFinder(OFFENDER_1, 3)
    whenever(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(pathFinder)

    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service.getExtremismProfile(
      OFFENDER_1,
      false,
    )

    assertThat(provisionalCategorisation).isEqualTo("C")
    assertThat(increasedRiskOfExtremism).isFalse
    assertThat(notifyRegionalCTLead).isTrue
  }

  @Test
  fun testWhenPathfinderOnFileWithBand4() {
    val pathFinder = PathFinder(OFFENDER_1, 4)
    whenever(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(pathFinder)

    val (
      _,
      provisionalCategorisation,
      notifyRegionalCTLead,
      increasedRiskOfExtremism) = service.getExtremismProfile(
      OFFENDER_1,
      false,
    )

    assertThat(provisionalCategorisation).isEqualTo("C")
    assertThat(increasedRiskOfExtremism).isFalse
    assertThat(notifyRegionalCTLead).isFalse
  }

  @Test
  fun testWhenPathfinderOnFileWithNoBand() {
    val pathFinder = PathFinder(OFFENDER_1, null)
    whenever(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(pathFinder)

    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service.getExtremismProfile(
      OFFENDER_1, false
    )

    assertThat(provisionalCategorisation).isEqualTo("C")
    assertThat(increasedRiskOfExtremism).isFalse
    assertThat(notifyRegionalCTLead).isFalse
  }

  @Test
  fun testWhenPathfinderNotOnFile() {
    whenever(pathfinderRepo.getBand(eq(OFFENDER_1))).thenReturn(null)

    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service.getExtremismProfile(
      OFFENDER_1, false
    )

    assertThat(provisionalCategorisation).isEqualTo("C")
    assertThat(increasedRiskOfExtremism).isFalse
    assertThat(notifyRegionalCTLead).isFalse
  }

  companion object {
    private const val OFFENDER_1 = "AB1234A"
  }
}
