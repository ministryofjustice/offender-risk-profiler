package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert

@ExtendWith(MockitoExtension::class)
class EscapeDecisionTreeServiceTest {
  private var service: EscapeDecisionTreeService? = null

  @Mock
  private val nomisService: NomisService? = null
  @BeforeEach
  fun setup() {
    service = EscapeDecisionTreeService(nomisService!!)
  }

  @Test
  fun testMixedResponse() {
    Mockito.`when`(nomisService!!.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(activeListAlert, activeRiskAlert, inactiveListAlert, inactiveRiskAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeListAlerts, escapeRiskAlerts) = service!!.getEscapeProfile(
      OFFENDER_1
    )
    Assertions.assertThat(
      escapeListAlerts
    ).hasSize(1)
    Assertions.assertThat(
      escapeRiskAlerts
    ).hasSize(1)
    Assertions.assertThat(activeEscapeList).isTrue
    Assertions.assertThat(activeEscapeRisk).isTrue
  }

  @Test
  fun testListResponse() {
    Mockito.`when`(nomisService!!.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(activeListAlert, activeListAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeListAlerts, escapeRiskAlerts) = service!!.getEscapeProfile(
      OFFENDER_1
    )
    Assertions.assertThat(
      escapeListAlerts
    ).hasSize(2)
    Assertions.assertThat(
      escapeRiskAlerts
    ).hasSize(0)
    Assertions.assertThat(activeEscapeList).isTrue
    Assertions.assertThat(activeEscapeRisk).isFalse
  }

  @Test
  fun testRiskResponse() {
    Mockito.`when`(nomisService!!.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(activeRiskAlert, activeRiskAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeListAlerts, escapeRiskAlerts) = service!!.getEscapeProfile(
      OFFENDER_1
    )
    Assertions.assertThat(
      escapeListAlerts
    ).hasSize(0)
    Assertions.assertThat(
      escapeRiskAlerts
    ).hasSize(2)
    Assertions.assertThat(activeEscapeList).isFalse
    Assertions.assertThat(activeEscapeRisk).isTrue
  }

  @Test
  fun testNoAlertsResponse() {
    Mockito.`when`(nomisService!!.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(listOf())
    val (_, _, activeEscapeList, activeEscapeRisk, escapeListAlerts, escapeRiskAlerts) = service!!.getEscapeProfile(
      OFFENDER_1
    )
    Assertions.assertThat(activeEscapeList).isFalse
    Assertions.assertThat(activeEscapeRisk).isFalse
    Assertions.assertThat(
      escapeListAlerts
    ).hasSize(0)
    Assertions.assertThat(
      escapeRiskAlerts
    ).hasSize(0)
  }

  @Test
  fun testInactiveOnlyAlertsResponse() {
    Mockito.`when`(nomisService!!.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(inactiveListAlert, inactiveRiskAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeListAlerts, escapeRiskAlerts) = service!!.getEscapeProfile(
      OFFENDER_1
    )
    Assertions.assertThat(activeEscapeList).isFalse
    Assertions.assertThat(activeEscapeRisk).isFalse
    Assertions.assertThat(
      escapeListAlerts
    ).hasSize(0)
    Assertions.assertThat(
      escapeRiskAlerts
    ).hasSize(0)
  }

  companion object {
    private const val OFFENDER_1 = "AB1234A"
    private val activeListAlert = Alert(true, "XEL")
    private val activeRiskAlert = Alert(true, "XER")
    private val inactiveRiskAlert = Alert(false, "XER")
    private val inactiveListAlert = Alert(false, "XEL")
  }
}
