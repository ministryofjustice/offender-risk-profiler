package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ExtremismDecisionTreeServiceTest {
  private var service: ExtremismDecisionTreeService? = null

  @Mock
  private val pathfinderRepo: PathfinderService? = null
  @BeforeEach
  fun setup() {
    service = ExtremismDecisionTreeService(pathfinderRepo!!)
  }

  @Test
  fun testWhenPathfinderOnFileWithBand1NoPreviousOffences() {
    val pathFinder = PathFinder(OFFENDER_1, 1)
    Mockito.`when`(
      pathfinderRepo!!.getBand(ArgumentMatchers.eq(OFFENDER_1))
    ).thenReturn(Optional.of(pathFinder))
    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service!!.getExtremismProfile(
      OFFENDER_1, false
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(increasedRiskOfExtremism).isTrue
    Assertions.assertThat(notifyRegionalCTLead).isTrue
  }

  @Test
  fun testWhenPathfinderOnFileWithBand2WithPreviousOffences() {
    val pathFinder = PathFinder(OFFENDER_1, 2)
    Mockito.`when`(
      pathfinderRepo!!.getBand(ArgumentMatchers.eq(OFFENDER_1))
    ).thenReturn(Optional.of(pathFinder))
    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service!!.getExtremismProfile(
      OFFENDER_1, true
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("B")
    Assertions.assertThat(increasedRiskOfExtremism).isTrue
    Assertions.assertThat(notifyRegionalCTLead).isTrue
  }

  @Test
  fun testWhenPathfinderOnFileWithBand3() {
    val pathFinder = PathFinder(OFFENDER_1, 3)
    Mockito.`when`(
      pathfinderRepo!!.getBand(ArgumentMatchers.eq(OFFENDER_1))
    ).thenReturn(Optional.of(pathFinder))
    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service!!.getExtremismProfile(
      OFFENDER_1, false
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(increasedRiskOfExtremism).isFalse
    Assertions.assertThat(notifyRegionalCTLead).isTrue
  }

  @Test
  fun testWhenPathfinderOnFileWithBand4() {
    val pathFinder = PathFinder(OFFENDER_1, 4)
    Mockito.`when`(
      pathfinderRepo!!.getBand(ArgumentMatchers.eq(OFFENDER_1))
    ).thenReturn(Optional.of(pathFinder))
    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service!!.getExtremismProfile(
      OFFENDER_1, false
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(increasedRiskOfExtremism).isFalse
    Assertions.assertThat(notifyRegionalCTLead).isFalse
  }

  @Test
  fun testWhenPathfinderOnFileWithNoBand() {
    val pathFinder = PathFinder(OFFENDER_1, null)
    Mockito.`when`(
      pathfinderRepo!!.getBand(ArgumentMatchers.eq(OFFENDER_1))
    ).thenReturn(Optional.of(pathFinder))
    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service!!.getExtremismProfile(
      OFFENDER_1, false
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(increasedRiskOfExtremism).isFalse
    Assertions.assertThat(notifyRegionalCTLead).isFalse
  }

  @Test
  fun testWhenPathfinderNotOnFile() {
    Mockito.`when`(
      pathfinderRepo!!.getBand(ArgumentMatchers.eq(OFFENDER_1))
    ).thenReturn(Optional.empty())
    val (_, provisionalCategorisation, notifyRegionalCTLead, increasedRiskOfExtremism) = service!!.getExtremismProfile(
      OFFENDER_1, false
    )
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(increasedRiskOfExtremism).isFalse
    Assertions.assertThat(notifyRegionalCTLead).isFalse
  }

  companion object {
    private const val OFFENDER_1 = "AB1234A"
  }
}
