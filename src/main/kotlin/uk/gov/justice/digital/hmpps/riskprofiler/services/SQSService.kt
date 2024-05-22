package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfileChange
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException

@Service
class SQSService(
  hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  @Qualifier("hmppsriskprofilechangequeue-sqs-client") private val hmppsOffenderSqsClient: SqsAsyncClient,
) {

  private val hmppsOffenderQueueUrl = hmppsQueueService.findByQueueId("hmppsriskprofilechangequeue")?.queueUrl ?: throw MissingQueueException("HmppsQueue hmppsoffenderqueue not found")

  fun sendRiskProfileChangeMessage(payload: RiskProfileChange) {

    val sendMessage =
      SendMessageRequest.builder()
        .queueUrl(hmppsOffenderQueueUrl)
        .messageBody(
          objectMapper.writeValueAsString(
            payload,
          ),
        ).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("RISK_PROFIlE_CHANGE").build()),
        ).build()
    log.info("publishing event type {}", "RISK_PROFIlE_CHANGE")

    hmppsOffenderSqsClient.sendMessage(sendMessage)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
