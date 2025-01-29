package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.riskprofiler.clent.PrisonerAlertsApiClient
import uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert.PrisonerAlertCodeSummaryDto
import uk.gov.justice.digital.hmpps.riskprofiler.factories.dto.prisonerAlert.TestPrisonerAlertCodeSummaryDtoFactory
import uk.gov.justice.digital.hmpps.riskprofiler.factories.dto.prisonerAlert.TestPrisonerAlertResponseDtoFactory
import uk.gov.justice.digital.hmpps.riskprofiler.model.*
import java.time.LocalDate

@RunWith(MockitoJUnitRunner::class)
class NomisServiceTest {
  private lateinit var service: NomisService

  @Mock
  private lateinit var webClientCallHelper: WebClientCallHelper

  @Mock
  private lateinit var prisonerAlertsApiClient: PrisonerAlertsApiClient

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(webClientCallHelper)
    MockitoAnnotations.openMocks(prisonerAlertsApiClient)
    service = NomisService(webClientCallHelper, prisonerAlertsApiClient, listOf("ASSAULTS"), listOf("ACTINV", "ASSIAL"))
  }

  @Throws(Exception::class)
  @Test
  fun testAlertCall() {
    val testAlertsResponse = listOf(
      (TestPrisonerAlertResponseDtoFactory())
        .withActive(false)
        .withActiveTo(LocalDate.now().minusMonths(1))
        .withAlertCodeSummary((TestPrisonerAlertCodeSummaryDtoFactory()).withAlertCode("SOC").build())
        .build()
    )
    Mockito.`when`(
      prisonerAlertsApiClient.findPrisonerAlerts(
        "A1234AA",
        listOf("SOC")
      ),
    )
      .thenReturn(
        RestPage(testAlertsResponse, 1, testAlertsResponse.size, 1)
      )
    val alertsForOffender = service.getAlertsForOffender("A1234AA", listOf("SOC"))
    Assertions.assertThat(alertsForOffender).hasSize(1)
    Mockito.verify(prisonerAlertsApiClient).findPrisonerAlerts(
      "A1234AA",
      listOf("SOC")
    )
    Mockito.verifyNoMoreInteractions(prisonerAlertsApiClient)
  }

  @Test
  @Throws(Exception::class)
  fun testBookingDetail() {
    val body = RestResponsePage<OffenderBooking>(
      listOf(OffenderBooking(1, "G1234H", "A1234AA", "LIFE")),
    )
    val response = ResponseEntity(body, HttpStatus.OK)
    Mockito.`when`(
      webClientCallHelper.getPageRestResponse(
        eq("/api/bookings/v2?bookingId=1&legalInfo=true"),
        isA<ParameterizedTypeReference<RestResponsePage<OffenderBooking>>>(),
      ),
    )
      .thenReturn(response)
    val bookingDetails = service.getBookingDetails(1L)
    Assertions.assertThat(bookingDetails).hasSize(1)
    Mockito.verify(webClientCallHelper).getPageRestResponse(
      eq("/api/bookings/v2?bookingId=1&legalInfo=true"),
      isA<ParameterizedTypeReference<RestResponsePage<OffenderBooking>>>(),
    )
    Mockito.verifyNoMoreInteractions(webClientCallHelper)
  }

  @Test
  @Throws(Exception::class)
  fun testGetOffendersInPrison() {
    val body = RestResponsePage<OffenderBooking>(
      listOf(
        OffenderBooking(1, "G1234H", "A1234AA", "LIFE"),
        OffenderBooking(2, "G1235H", "A1234AB", "LIFE"),
        OffenderBooking(3, "G1236H", "A1234AC", "LIFE"),
        OffenderBooking(4, "G1237H", "A1234AD", "LIFE"),
        OffenderBooking(5, "G1238H", "A1234AE", "LIFE"),
      ),
    )
    val response = ResponseEntity(body, HttpStatus.OK)
    Mockito.`when`(
      webClientCallHelper.getPageRestResponse(
        eq("/api/bookings/v2?prisonId=MDI&size=4000"),
        isA<ParameterizedTypeReference<RestResponsePage<OffenderBooking>>>(),
      ),
    )
      .thenReturn(response)
    val offenderList = service.getOffendersAtPrison("MDI")
    Assertions.assertThat(offenderList).hasSize(5)
    Mockito.verify(webClientCallHelper).getPageRestResponse(
      eq("/api/bookings/v2?prisonId=MDI&size=4000"),
      isA<ParameterizedTypeReference<RestResponsePage<OffenderBooking>>>(),
    )
    Mockito.verifyNoMoreInteractions(webClientCallHelper)
  }

  @Test
  @Throws(Exception::class)
  fun testEscapeListCall() {
    val testEscapeRiskAlertsResponse = (TestPrisonerAlertResponseDtoFactory())
        .withActive(false)
        .withActiveTo(LocalDate.now().minusMonths(1))
        .withAlertCodeSummary((TestPrisonerAlertCodeSummaryDtoFactory()).withAlertCode(PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_RISK).build())
        .build()
    val testEscapeListAlertsResponse = (TestPrisonerAlertResponseDtoFactory())
        .withActive(false)
        .withActiveTo(LocalDate.now().minusMonths(1))
        .withAlertCodeSummary((TestPrisonerAlertCodeSummaryDtoFactory()).withAlertCode(PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_LIST).build())
        .build()
    val testAlertsResponse = listOf(testEscapeRiskAlertsResponse, testEscapeListAlertsResponse)

    Mockito.`when`(
      prisonerAlertsApiClient.findPrisonerAlerts(
        "A1234AA",
        listOf(PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_RISK, PrisonerAlertCodeSummaryDto.ALERT_CODE_ESCAPE_LIST)
      ),
    )
      .thenReturn(
        RestPage(testAlertsResponse, 1, testAlertsResponse.size, 2)
      )

    val alertsForOffender = service.getEscapeListAlertsForOffender("A1234AA")
    Assertions.assertThat(alertsForOffender).hasSize(2)
    Mockito.verify(webClientCallHelper).getForList(
      eq("/api/offenders/A1234AA/alerts/v2?alertCode=XER,XEL"),
      isA<ParameterizedTypeReference<List<Alert>>>(),
    )
    Mockito.verifyNoMoreInteractions(webClientCallHelper)
  }

  @Test
  @Throws(Exception::class)
  fun testIncidentCall() {
    val body = listOf(IncidentCase(), IncidentCase())
    val response = ResponseEntity(body, HttpStatus.OK)
    Mockito.`when`(
      webClientCallHelper.getForList(
        eq("/api/offenders/A1234AA/incidents?incidentType=ASSAULTS&participationRoles=ACTINV&participationRoles=ASSIAL"),
        isA<ParameterizedTypeReference<List<IncidentCase>>>(),
      ),
    )
      .thenReturn(response)
    val incidentsForOffender = service.getIncidents("A1234AA")
    Assertions.assertThat(incidentsForOffender).hasSize(2)
    Mockito.verify(webClientCallHelper).getForList(
      eq("/api/offenders/A1234AA/incidents?incidentType=ASSAULTS&participationRoles=ACTINV&participationRoles=ASSIAL"),
      isA<ParameterizedTypeReference<List<IncidentCase>>>(),
    )
    Mockito.verifyNoMoreInteractions(webClientCallHelper)
  }

  @Test
  @Throws(Exception::class)
  fun testGetPartiesOfIncidentHappy() {
    val incidentParty1 = IncidentParty(12345L, null, null, null, null, null, null, null)
    val incidentParty2 = IncidentParty(12346L, null, null, null, null, null, null, null)
    val incidentCase = IncidentCase(123L)
    incidentCase.incidentType = "ASSAULTS"
    incidentCase.parties = listOf(incidentParty1, incidentParty2)
    Mockito.`when`(webClientCallHelper.get("/api/incidents/123", IncidentCase::class.java)).thenReturn(incidentCase)
    val bookingDetails1 = BookingDetails(12345L)
    bookingDetails1.offenderNo = "OFFENDER1"
    Mockito.`when`(webClientCallHelper.get("/api/bookings/12345?basicInfo=true", BookingDetails::class.java))
      .thenReturn(bookingDetails1)
    val bookingDetails2 = BookingDetails(12346L)
    bookingDetails2.offenderNo = "OFFENDER2"
    Mockito.`when`(webClientCallHelper.get("/api/bookings/12346?basicInfo=true", BookingDetails::class.java))
      .thenReturn(bookingDetails2)
    val partiesOfIncident = service.getPartiesOfIncident(123L)
    Assertions.assertThat(partiesOfIncident).asList().containsExactly("OFFENDER1", "OFFENDER2")
  }

  @Test
  @Throws(Exception::class)
  fun testGetPartiesOfIncidentIrrelevantType() {
    val incidentParty = IncidentParty(12345L, null, null, null, null, null, null, null)
    val incidentCase = IncidentCase(123L)
    incidentCase.incidentType = "OTHER"
    incidentCase.parties = listOf(incidentParty)
    Mockito.`when`(webClientCallHelper.get("/api/incidents/123", IncidentCase::class.java)).thenReturn(incidentCase)
    val partiesOfIncident = service.getPartiesOfIncident(123L)
    Assertions.assertThat(partiesOfIncident).asList().isEmpty()
  }

  @Test
  @Throws(Exception::class)
  fun testGetPartiesOfIncidentNoBookingId() {
    val incidentParty = IncidentParty()
    val incidentCase = IncidentCase(123L)
    incidentCase.incidentType = "ASSAULTS"
    incidentCase.parties = listOf(incidentParty)
    Mockito.`when`(webClientCallHelper.get("/api/incidents/123", IncidentCase::class.java)).thenReturn(incidentCase)
    val partiesOfIncident = service.getPartiesOfIncident(123L)
    Assertions.assertThat(partiesOfIncident).asList().isEmpty()
  }

  @Test
  @Throws(Exception::class)
  fun testGetPartiesOfIncidentNoParties() {
    val incidentCase = IncidentCase(123L)
    incidentCase.incidentType = "ASSAULTS"
    Mockito.`when`(webClientCallHelper.get("/api/incidents/123", IncidentCase::class.java)).thenReturn(incidentCase)
    val partiesOfIncident = service.getPartiesOfIncident(123L)
    Assertions.assertThat(partiesOfIncident).asList().isEmpty()
  }

  @Test
  @Throws(Exception::class)
  fun testGetPartiesOfIncident404() {
    Mockito.`when`(webClientCallHelper.get("/api/incidents/123", IncidentCase::class.java)).thenThrow(
      WebClientResponseException.create(HttpStatus.NOT_FOUND.value(), "test", null, null, null),
    )
    val partiesOfIncident = service.getPartiesOfIncident(123L)
    Assertions.assertThat(partiesOfIncident).asList().isEmpty()
  }
}
