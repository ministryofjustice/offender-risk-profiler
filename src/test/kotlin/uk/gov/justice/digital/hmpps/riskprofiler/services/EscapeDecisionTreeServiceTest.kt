package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert.PrisonerAlertCodeSummaryDto
import uk.gov.justice.digital.hmpps.riskprofiler.factories.model.TestAlertFactory
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class EscapeDecisionTreeServiceTest {
  private lateinit var service: EscapeDecisionTreeService

  @Mock
  private lateinit var nomisService: NomisService

  @BeforeEach
  fun setup() {
    service = EscapeDecisionTreeService(nomisService)
  }

  @Test
  fun testMixedResponse() {
    Mockito.`when`(nomisService.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(activeListAlert, activeRiskAlert, inactiveListAlert, inactiveRiskAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeRiskAlerts, escapeListAlerts) = service.getEscapeProfile(
      OFFENDER_1,
    )
    Assertions.assertThat(escapeListAlerts).hasSize(1)
    Assertions.assertThat(escapeRiskAlerts).hasSize(1)
    Assertions.assertThat(activeEscapeList).isTrue
    Assertions.assertThat(activeEscapeRisk).isTrue
  }

  @Test
  fun testListResponse() {
    Mockito.`when`(nomisService.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(activeListAlert, activeListAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeRiskAlerts, escapeListAlerts) = service.getEscapeProfile(
      OFFENDER_1,
    )
    Assertions.assertThat(escapeListAlerts).hasSize(2)
    Assertions.assertThat(escapeRiskAlerts).hasSize(0)
    Assertions.assertThat(activeEscapeList).isTrue
    Assertions.assertThat(activeEscapeRisk).isFalse
  }

  @Test
  fun testRiskResponse() {
    Mockito.`when`(nomisService.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(activeRiskAlert, activeRiskAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeRiskAlerts, escapeListAlerts) = service.getEscapeProfile(
      OFFENDER_1,
    )
    Assertions.assertThat(escapeListAlerts).hasSize(0)
    Assertions.assertThat(escapeRiskAlerts).hasSize(2)
    Assertions.assertThat(activeEscapeList).isFalse
    Assertions.assertThat(activeEscapeRisk).isTrue
  }

  @Test
  fun testNoAlertsResponse() {
    Mockito.`when`(nomisService.getEscapeListAlertsForOffender(OFFENDER_1)).thenReturn(listOf())
    val (_, _, activeEscapeList, activeEscapeRisk, escapeRiskAlerts, escapeListAlerts) = service.getEscapeProfile(
      OFFENDER_1,
    )
    Assertions.assertThat(activeEscapeList).isFalse
    Assertions.assertThat(activeEscapeRisk).isFalse
    Assertions.assertThat(escapeListAlerts).hasSize(0)
    Assertions.assertThat(escapeRiskAlerts).hasSize(0)
  }

  @Test
  fun testInactiveOnlyAlertsResponse() {
    Mockito.`when`(nomisService.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(inactiveListAlert, inactiveRiskAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeRiskAlerts, escapeListAlerts) = service.getEscapeProfile(
      OFFENDER_1,
    )
    Assertions.assertThat(activeEscapeList).isFalse
    Assertions.assertThat(activeEscapeRisk).isFalse
    Assertions.assertThat(escapeListAlerts).hasSize(0)
    Assertions.assertThat(escapeRiskAlerts).hasSize(0)
  }

  @Test
  fun testExpiredOnlyAlertsResponse() {
    Mockito.`when`(nomisService.getEscapeListAlertsForOffender(OFFENDER_1))
      .thenReturn(listOf(expiredListAlert))
    val (_, _, activeEscapeList, activeEscapeRisk, escapeRiskAlerts, escapeListAlerts) = service.getEscapeProfile(
      OFFENDER_1,
    )
    Assertions.assertThat(activeEscapeList).isFalse
    Assertions.assertThat(activeEscapeRisk).isFalse
    Assertions.assertThat(escapeListAlerts).hasSize(0)
    Assertions.assertThat(escapeRiskAlerts).hasSize(0)
  }

  companion object {
    private const val OFFENDER_1 = "AB1234A"
    private val activeListAlert = (TestAlertFactory())
      .withActive(true)
      .withActiveFrom(LocalDate.now().minusMonths(1))
      .withDateExpires(LocalDate.now().plusMonths(1))
      .withAlertCode(PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_LIST)
      .build()
    private val activeRiskAlert = (TestAlertFactory())
      .withActive(true)
      .withActiveFrom(LocalDate.now().minusMonths(1))
      .withDateExpires(LocalDate.now().plusMonths(1))
      .withAlertCode(PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_RISK)
      .build()
    private val inactiveListAlert = (TestAlertFactory())
      .withActive(false)
      .withActiveFrom(LocalDate.now().minusMonths(1))
      .withDateExpires(LocalDate.now().plusMonths(1))
      .withAlertCode(PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_LIST)
      .build()
    private val inactiveRiskAlert = (TestAlertFactory())
      .withActive(false)
      .withActiveFrom(LocalDate.now().minusMonths(1))
      .withDateExpires(LocalDate.now().plusMonths(1))
      .withAlertCode(PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_RISK)
      .build()
    private val expiredListAlert = (TestAlertFactory())
      .withActive(true)
      .withActiveFrom(LocalDate.now().minusMonths(2))
      .withDateExpires(LocalDate.now().minusMonths(1))
      .withAlertCode(PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_LIST)
      .build()
  }
}
