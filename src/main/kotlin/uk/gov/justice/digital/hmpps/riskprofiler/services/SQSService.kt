package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfileChange
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.util.function.Consumer

@Service
class SQSService(
  hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper
) {

  //private final val riskProfileChangeQueueSqsClient: SqsAsyncClient


  init {
  //  val riskProfileChangeQueue = hmppsQueueService.findByQueueId("riskprofilechangequeue") ?: throw MissingQueueException("Could not find queue riskprofilechangequeue")

 //   riskProfileChangeQueueSqsClient = riskProfileChangeQueue.sqsClient
  }

  fun sendRiskProfileChangeMessage(payload: RiskProfileChange) {

    try {
      val request =
        SendMessageRequest.builder().messageBody(objectMapper.writeValueAsString(payload))

      val r: Consumer<SendMessageRequest.Builder>? = Consumer {
        SendMessageRequest.builder().messageBody(objectMapper.writeValueAsString(payload))
      }

    //  riskProfileChangeQueueSqsClient.sendMessage(r)

    } catch (e: JsonProcessingException) {
      log.error("Failed to convert payload {} to json", payload)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
