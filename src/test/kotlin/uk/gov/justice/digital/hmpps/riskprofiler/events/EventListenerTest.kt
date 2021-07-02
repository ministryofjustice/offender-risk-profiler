package uk.gov.justice.digital.hmpps.riskprofiler.events

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService
import uk.gov.justice.digital.hmpps.riskprofiler.services.PollPrisonersService
import java.util.Arrays

@ExtendWith(MockitoExtension::class)
class EventListenerTest {
  private lateinit var service: EventListener

  @Mock
  private lateinit var nomisService: NomisService

  @Mock
  private lateinit var pollPrisonersService: PollPrisonersService

  private val objectMapper = ObjectMapper()

  @BeforeEach
  fun setup() {
    service = EventListener(nomisService, pollPrisonersService, objectMapper)
  }

  @Test
  fun testAlertsEscape() {
    Mockito.`when`(nomisService.getOffender(BOOKING_1)).thenReturn(OFFENDER_1)
    service.eventListener("{ \"Message\":\"{ \\\"eventType\\\":\\\"ALERT-INSERTED\\\", \\\"alertCode\\\":\\\"XER\\\", \\\"bookingId\\\":" + BOOKING_1 + " }\"}")
    Mockito.verify(nomisService).evictEscapeListAlertsCache(OFFENDER_1)
    Mockito.verify(nomisService, Mockito.never()).evictSocListAlertsCache(OFFENDER_1)
    Mockito.verify(pollPrisonersService).pollPrisoner(OFFENDER_1)
    Mockito.verify(nomisService, Mockito.never()).getPartiesOfIncident(ArgumentMatchers.any())
  }

  @Test
  fun testAlertsSoc() {
    Mockito.`when`(nomisService.getOffender(BOOKING_1)).thenReturn(OFFENDER_1)
    service.eventListener("{ \"Message\":\"{ \\\"eventType\\\":\\\"ALERT-UPDATED\\\", \\\"alertCode\\\":\\\"XEAN\\\", \\\"bookingId\\\":" + BOOKING_1 + " }\"}")
    Mockito.verify(nomisService, Mockito.never()).evictEscapeListAlertsCache(OFFENDER_1)
    Mockito.verify(nomisService).evictSocListAlertsCache(OFFENDER_1)
    Mockito.verify(pollPrisonersService).pollPrisoner(OFFENDER_1)
    Mockito.verify(nomisService, Mockito.never()).getPartiesOfIncident(ArgumentMatchers.any())
  }

  @Test
  fun testAlertsIrrelevant() {
    service.eventListener("{ \"Message\":\"{ \\\"eventType\\\":\\\"ALERT-INSERTED\\\", \\\"alertCode\\\":\\\"OTHER\\\", \\\"bookingId\\\":" + BOOKING_1 + " }\"}")
    Mockito.verify(nomisService, Mockito.never()).evictEscapeListAlertsCache(ArgumentMatchers.any())
    Mockito.verify(nomisService, Mockito.never()).evictSocListAlertsCache(ArgumentMatchers.any())
    Mockito.verify(pollPrisonersService, Mockito.never()).pollPrisoner(ArgumentMatchers.anyString())
    Mockito.verify(nomisService, Mockito.never()).getPartiesOfIncident(ArgumentMatchers.any())
  }

  @Test
  fun testIncidents() {
    Mockito.`when`(nomisService.getPartiesOfIncident(INCIDENT_1)).thenReturn(Arrays.asList(OFFENDER_1, OFFENDER_2))
    service.eventListener("{ \"Message\": \"{ \\\"eventType\\\":\\\"INCIDENT-CHANGED-CASES\\\", \\\"incidentCaseId\\\":" + INCIDENT_1 + " }\"}")
    Mockito.verify(nomisService).evictIncidentsCache(OFFENDER_1)
    Mockito.verify(nomisService).evictIncidentsCache(OFFENDER_2)
    Mockito.verify(nomisService, Mockito.never()).getOffender(ArgumentMatchers.any())
  }

  @Test
  fun testInvalidMessage() {
    service.eventListener("text contents")
    Mockito.verify(nomisService, Mockito.never()).getOffender(ArgumentMatchers.any())
    Mockito.verify(nomisService, Mockito.never()).getPartiesOfIncident(ArgumentMatchers.any())
  }

  companion object {
    private const val OFFENDER_1 = "AB1234A"
    private const val OFFENDER_2 = "AB1234B"
    private const val BOOKING_1 = 123456L
    private const val INCIDENT_1 = 456123L
  }
}
