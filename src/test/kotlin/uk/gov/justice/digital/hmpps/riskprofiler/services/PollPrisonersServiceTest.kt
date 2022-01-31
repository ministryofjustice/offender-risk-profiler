package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argThat
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PreviousProfileRepository
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.PreviousProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.ProfileMessagePayload
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfileChange
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.ViolenceProfile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class PollPrisonersServiceTest {
  private lateinit var service: PollPrisonersService

  @Mock
  private lateinit var socDecisionTreeService: SocDecisionTreeService

  @Mock
  private lateinit var violenceDecisionTreeService: ViolenceDecisionTreeService

  @Mock
  private lateinit var escapeDecisionTreeService: EscapeDecisionTreeService

  @Mock
  private lateinit var previousProfileRepository: PreviousProfileRepository

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Mock
  private lateinit var sqsService: SQSService

  private val SOC_1 = SocProfile(OFFENDER_1, "C", false)
  private val VIOLENCE_1 = ViolenceProfile(OFFENDER_1, "C", false, false, false, 0, 0, 0)
  private val ESCAPE_1 = EscapeProfile(
    OFFENDER_1, "C", false,
    true,
    listOf(Alert(alertCode = "XER", dateCreated = LocalDate.parse("2016-01-13"), active = true, expired = false)),
    null
  )
  private val PROFILE_1 = PreviousProfile(
    OFFENDER_1,
    """{"nomsId":"$OFFENDER_1","provisionalCategorisation":"C","activeEscapeList":false,"activeEscapeRisk":true,"escapeRiskAlerts":[{"alertCode":"XER","dateCreated":"2016-01-13","expired":false,"active":true}],"riskType":"ESCAPE"}""",
    """{"nomsId":"$OFFENDER_1","provisionalCategorisation":"C","transferToSecurity":false,"riskType":"SOC"}""",
    """{"nomsId":"$OFFENDER_1","provisionalCategorisation":"C","veryHighRiskViolentOffender":false,"notifySafetyCustodyLead":false,"displayAssaults":false,"numberOfAssaults":0,"numberOfSeriousAssaults":0,"numberOfNonSeriousAssaults":0,"riskType":"VIOLENCE"}""",
    LocalDateTime.now()
  )

  @Before
  fun setup() {
    service = PollPrisonersService(
      socDecisionTreeService,
      violenceDecisionTreeService,
      escapeDecisionTreeService,
      previousProfileRepository,
      telemetryClient,
      sqsService
    )
  }

  @Test
  fun testChangeSoc() {
    Mockito.`when`(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SocProfile(OFFENDER_1, "C", true))
    Mockito.`when`(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1)
    Mockito.`when`(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1)
    Mockito.`when`(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.of(PROFILE_1))
    service.pollPrisoner(OFFENDER_1)
    val rpc = RiskProfileChange(
      ProfileMessagePayload(ESCAPE_1, SOC_1, VIOLENCE_1),
      ProfileMessagePayload(ESCAPE_1, SocProfile(OFFENDER_1, "C", true), VIOLENCE_1),
      OFFENDER_1,
      LocalDateTime.now()
    )
    Mockito.verify(sqsService).sendRiskProfileChangeMessage(eqRiskProfileChange(rpc))
    Mockito.verify(previousProfileRepository, Mockito.never()).save(ArgumentMatchers.any())
  }

  @Test
  fun testNoChange() {
    Mockito.`when`(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SOC_1)
    Mockito.`when`(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1)
    Mockito.`when`(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1)
    Mockito.`when`(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.of(PROFILE_1))

    service.pollPrisoner(OFFENDER_1)

    Mockito.verify(sqsService, Mockito.never())
      .sendRiskProfileChangeMessage(ArgumentMatchers.isA(RiskProfileChange::class.java))
    Mockito.verify(previousProfileRepository, Mockito.never()).save(ArgumentMatchers.any())
  }

  @Test
  fun testNewData() {
    Mockito.`when`(socDecisionTreeService.getSocData(OFFENDER_1)).thenReturn(SOC_1)
    Mockito.`when`(violenceDecisionTreeService.getViolenceProfile(OFFENDER_1)).thenReturn(VIOLENCE_1)
    Mockito.`when`(escapeDecisionTreeService.getEscapeProfile(OFFENDER_1)).thenReturn(ESCAPE_1)
    Mockito.`when`(previousProfileRepository.findById(OFFENDER_1)).thenReturn(Optional.empty())
    service.pollPrisoner(OFFENDER_1)
    Mockito.verify(socDecisionTreeService).getSocData(OFFENDER_1)
    Mockito.verify(previousProfileRepository).save(ArgumentMatchers.any())
    Mockito.verify(previousProfileRepository).save(eqProfiles(PROFILE_1))
    Mockito.verify(sqsService, Mockito.never())
      .sendRiskProfileChangeMessage(ArgumentMatchers.isA(RiskProfileChange::class.java))
  }

  companion object {
    private const val OFFENDER_1 = "AB1234A"
    private fun eqProfiles(profile: PreviousProfile): PreviousProfile {
      return argThat { actual ->
        profile.soc == actual.soc && profile.violence == actual.violence && profile.escape == actual.escape && Math.abs(
          ChronoUnit.SECONDS.between(profile.executeDateTime, actual.executeDateTime)
        ) < 2
      }
    }

    private fun eqRiskProfileChange(rpc: RiskProfileChange): RiskProfileChange {
      return argThat { (oldProfile, newProfile, offenderNo, executeDateTime) ->
        rpc.newProfile == newProfile && rpc.oldProfile == oldProfile && Math.abs(
          ChronoUnit.SECONDS.between(rpc.executeDateTime, executeDateTime)
        ) < 2 && rpc.offenderNo == offenderNo
      }
    }
  }
}
