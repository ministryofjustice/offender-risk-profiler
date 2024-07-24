package uk.gov.justice.digital.hmpps.riskprofiler.events

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.QueueAttributes
import io.opentelemetry.api.trace.SpanKind
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderEvent
import uk.gov.justice.digital.hmpps.riskprofiler.services.NomisService
import uk.gov.justice.digital.hmpps.riskprofiler.services.PollPrisonersService
import java.io.IOException
import java.util.function.Consumer

@Service
class EventListener(
  private val nomisService: NomisService,
  private val pollPrisonersService: PollPrisonersService,
  private val objectMapper: ObjectMapper
) {
 // @SqsListener("events", factory = "hmppsQueueContainerFactoryProxy")
  @Throws(JsonProcessingException::class)
  fun onOffenderEvent(message: String, attributes: QueueAttributes) {

    val event = getOffenderEvent(message)

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
          } else {

          }
        }
        "INCIDENT-INSERTED", "INCIDENT-CHANGED-CASES", "INCIDENT-CHANGED-PARTIES", "INCIDENT-CHANGED-RESPONSES", "INCIDENT-CHANGED-REQUIREMENTS" -> {
          val nomsIds = nomisService.getPartiesOfIncident(event.incidentCaseId)
          // TODO: Do not poll immediately (leave for batch) as one incident generates a lot of events
          // Also should apply filter choosing only the relevant types of Assault incidents
          nomsIds.forEach(Consumer { nomsId: String? -> nomisService.evictIncidentsCache(nomsId) })
        }

        else -> {  // What should we do?
        }
      }
    } else { // What should we do?
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

data class SQSMessage(val Message: String, val MessageId: String, val MessageAttributes: MessageAttributes)
data class TypeValuePair(val Value: String, val Type: String)
data class HmppsEvent(val id: String, val type: String, val contents: String)
data class EventType(val Value: String, val Type: String)
data class MessageAttributes(val eventType: EventType)
data class Message(
  val Message: String,
  val MessageId: String,
  val MessageAttributes: MessageAttributes,
)
