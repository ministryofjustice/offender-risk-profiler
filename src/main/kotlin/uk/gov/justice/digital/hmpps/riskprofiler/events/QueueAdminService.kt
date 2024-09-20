package uk.gov.justice.digital.hmpps.riskprofiler.events

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class QueueAdminService(
  @Qualifier("awsClientForEvents") private val eventAwsSqsClient: AmazonSQS,
  @Qualifier("awsDlqClientForEvents") private val eventAwsSqsDlqClient: AmazonSQS,
  private val telemetryClient: TelemetryClient?,
  @Value("\${sqs.events.queue.name}") private val eventQueueName: String,
  @Value("\${sqs.events.dlq.queue.name}") private val eventDlqName: String,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  enum class TelemetryEvents {
    PURGED_EVENT_DLQ, TRANSFERRED_EVENT_DLQ
  }

  private val eventQueueUrl: String by lazy { eventAwsSqsClient.getQueueUrl(eventQueueName).queueUrl }
  private val eventDlqUrl: String by lazy { eventAwsSqsDlqClient.getQueueUrl(eventDlqName).queueUrl }

  fun transferEventMessages() {
    getEventDlqMessageCount()
      .takeIf { it > 0 }
      ?.also { total ->
        repeat(total) {
          eventAwsSqsDlqClient.receiveMessage(ReceiveMessageRequest(eventDlqUrl).withMaxNumberOfMessages(1)).messages
            .forEach { msg ->
              eventAwsSqsClient.sendMessage(eventQueueUrl, msg.body)
              eventAwsSqsDlqClient.deleteMessage(DeleteMessageRequest(eventDlqUrl, msg.receiptHandle))
              log.info("Transferred message from Event DLQ: $msg")
            }
        }
        telemetryClient?.trackEvent(
          TelemetryEvents.TRANSFERRED_EVENT_DLQ.name,
          mapOf("messages-on-queue" to total.toString()),
          null,
        )
      }
  }

  private fun getEventDlqMessageCount() =
    eventAwsSqsDlqClient.getQueueAttributes(eventDlqUrl, listOf("ApproximateNumberOfMessages"))
      .attributes["ApproximateNumberOfMessages"]
      ?.toInt() ?: 0
}
