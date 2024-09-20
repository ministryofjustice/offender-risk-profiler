package uk.gov.justice.digital.hmpps.riskprofiler.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderEvent
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService
import uk.gov.justice.digital.hmpps.riskprofiler.services.PollPrisonersService
import java.io.IOException
import java.util.function.Consumer

@Service
@ConditionalOnProperty(name = ["sqs.provider"])
class EventListener(
  private val nomisService: NomisService,
  private val pollPrisonersService: PollPrisonersService,
  private val objectMapper: ObjectMapper,
) {
  @JmsListener(destination = "\${sqs.events.queue.name}")
  fun eventListener(requestJson: String) {
    val event = getOffenderEvent(requestJson)
    if (event != null) {
      when (event.eventType) {
        "ALERT-INSERTED", "ALERT-UPDATED", "ALERT-DELETED" -> {
          val isEscape = NomisService.ESCAPE_LIST_ALERT_TYPES.contains(event.alertCode)
          val isSoc = NomisService.SOC_ALERT_TYPES.contains(event.alertCode)
          if ((isEscape || isSoc) && event.bookingId != null) {
            // booking id can be null for ALERT-DELETED. In this case ignore, as it is unlikely it will trigger need to change to cat B
            val nomsId = nomisService.getOffender(event.bookingId)!!
            if (isEscape) {
              nomisService.evictEscapeListAlertsCache(nomsId)
            }
            if (isSoc) {
              nomisService.evictSocListAlertsCache(nomsId)
            }
            pollPrisonersService.pollPrisoner(nomsId)
          }
        }
        "INCIDENT-INSERTED", "INCIDENT-CHANGED-CASES", "INCIDENT-CHANGED-PARTIES", "INCIDENT-CHANGED-RESPONSES", "INCIDENT-CHANGED-REQUIREMENTS" -> {
          val nomsIds = nomisService.getPartiesOfIncident(event.incidentCaseId)
          // TODO: Do not poll immediately (leave for batch) as one incident generates a lot of events
          // Also should apply filter choosing only the relevant types of Assault incidents
          nomsIds.forEach(Consumer { nomsId: String? -> nomisService.evictIncidentsCache(nomsId) })
        }
      }
    }
  }

  private fun getOffenderEvent(requestJson: String): OffenderEvent? {
    var event: OffenderEvent? = null
    try {
      val message: Map<String, Any?> = objectMapper.readValue(requestJson)
      if (message["Message"] == null) {
        log.warn(requestJson)
      } else {
        val body = message["Message"] as String
        log.debug(body) // do not log an excessive amount of data
        event = objectMapper.readValue(body)
      }
    } catch (e: IOException) {
      log.error("Failed to Parse Message {} {}", requestJson, e)
    }
    return event
  }

  companion object {
    private val log = LoggerFactory.getLogger(EventListener::class.java)
  }
}
