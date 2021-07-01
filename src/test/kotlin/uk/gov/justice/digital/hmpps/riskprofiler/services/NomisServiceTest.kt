package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert
import uk.gov.justice.digital.hmpps.riskprofiler.model.BookingDetails
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentParty

@RunWith(MockitoJUnitRunner::class)
class NomisServiceTest {
  private lateinit var service: NomisService

  @Mock
  private lateinit var webClientCallHelper: WebClientCallHelper

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(webClientCallHelper)
    service = NomisService(webClientCallHelper, listOf("ASSAULTS"), listOf("ACTINV", "ASSIAL"))
  }

  @Test
  @Throws(Exception::class)
  fun testAlertCall() {
    val body = listOf(Alert(false, "SOC"))
    val response = ResponseEntity(body, HttpStatus.OK)
    Mockito.`when`(
      webClientCallHelper.getForList(
        eq("/api/offenders/A1234AA/alerts?query=alertCode:eq:'SOC'&latestOnly=false"),
        isA<ParameterizedTypeReference<List<Alert>>>()
      )
    )
      .thenReturn(response)
    val alertsForOffender = service.getAlertsForOffender("A1234AA", listOf("SOC"))
    Assertions.assertThat(alertsForOffender).hasSize(1)
    Mockito.verify(webClientCallHelper).getForList(
      eq("/api/offenders/A1234AA/alerts?query=alertCode:eq:'SOC'&latestOnly=false"),
      isA<ParameterizedTypeReference<List<Alert>>>()
    )
    Mockito.verifyNoMoreInteractions(webClientCallHelper)
  }

  @Test
  @Throws(Exception::class)
  fun testEscapeListCall() {
    val body = listOf(Alert(false, "XER"), Alert(false, "XEL"))
    val response = ResponseEntity(body, HttpStatus.OK)
    Mockito.`when`(
      webClientCallHelper.getForList(
        eq("/api/offenders/A1234AA/alerts?query=alertCode:eq:'XER',or:alertCode:eq:'XEL'&latestOnly=false"),
        isA<ParameterizedTypeReference<List<Alert>>>()
      )
    )
      .thenReturn(response)
    val alertsForOffender = service.getEscapeListAlertsForOffender("A1234AA")
    Assertions.assertThat(alertsForOffender).hasSize(2)
    Mockito.verify(webClientCallHelper).getForList(
      eq("/api/offenders/A1234AA/alerts?query=alertCode:eq:'XER',or:alertCode:eq:'XEL'&latestOnly=false"),
      isA<ParameterizedTypeReference<List<Alert>>>()
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
        isA<ParameterizedTypeReference<List<IncidentCase>>>()
      )
    )
      .thenReturn(response)
    val incidentsForOffender = service.getIncidents("A1234AA")
    Assertions.assertThat(incidentsForOffender).hasSize(2)
    Mockito.verify(webClientCallHelper).getForList(
      eq("/api/offenders/A1234AA/incidents?incidentType=ASSAULTS&participationRoles=ACTINV&participationRoles=ASSIAL"),
      isA<ParameterizedTypeReference<List<IncidentCase>>>()
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
      WebClientResponseException.create(HttpStatus.NOT_FOUND.value(), "test", null, null, null)
    )
    val partiesOfIncident = service.getPartiesOfIncident(123L)
    Assertions.assertThat(partiesOfIncident).asList().isEmpty()
  }
}
