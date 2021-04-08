package uk.gov.justice.digital.hmpps.riskprofiler.events

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.GetQueueAttributesResult
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.microsoft.applicationinsights.TelemetryClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class QueueAdminServiceTest {

    private val eventAwsSqsClient = mock<AmazonSQS>()
    private val eventAwsSqsDlqClient = mock<AmazonSQS>()
    private val telemetryClient = mock<TelemetryClient>()
    private lateinit var queueAdminService: QueueAdminService

    @BeforeEach
    internal fun setUp() {
        whenever(eventAwsSqsClient.getQueueUrl("event-queue")).thenReturn(GetQueueUrlResult().withQueueUrl("arn:eu-west-1:event-queue"))
        whenever(eventAwsSqsDlqClient.getQueueUrl("event-dlq")).thenReturn(GetQueueUrlResult().withQueueUrl("arn:eu-west-1:event-dlq"))
        queueAdminService = QueueAdminService(
            eventAwsSqsClient = eventAwsSqsClient,
            eventAwsSqsDlqClient = eventAwsSqsDlqClient,
            telemetryClient = telemetryClient,
            eventQueueName = "event-queue",
            eventDlqName = "event-dlq",
        )
    }

    @Nested
    inner class TransferAllEventDlqMessages {

        private val eventQueueUrl = "arn:eu-west-1:event-queue"
        private val eventDlqUrl = "arn:eu-west-1:event-dlq"

        @Test
        internal fun `will read single message from event dlq`() {
            stubDlqMessageCount(1)
            whenever(eventAwsSqsDlqClient.receiveMessage(any<ReceiveMessageRequest>()))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234AA"))))

            queueAdminService.transferEventMessages()

            verify(eventAwsSqsDlqClient).receiveMessage(
                check<ReceiveMessageRequest> {
                    assertThat(it.queueUrl).isEqualTo(eventDlqUrl)
                }
            )
        }

        @Test
        internal fun `will read multiple messages from dlq`() {
            stubDlqMessageCount(3)
            whenever(eventAwsSqsDlqClient.receiveMessage(any<ReceiveMessageRequest>()))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234AA"))))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234BB"))))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234CC"))))

            queueAdminService.transferEventMessages()

            verify(eventAwsSqsDlqClient, times(3)).receiveMessage(
                check<ReceiveMessageRequest> {
                    assertThat(it.queueUrl).isEqualTo(eventDlqUrl)
                }
            )
        }

        @Test
        internal fun `will send single message to the event queue`() {
            stubDlqMessageCount(1)
            whenever(eventAwsSqsDlqClient.receiveMessage(any<ReceiveMessageRequest>()))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234AA"))))

            queueAdminService.transferEventMessages()

            verify(eventAwsSqsClient).sendMessage(eventQueueUrl, dataComplianceDeleteOffenderMessage("Z1234AA"))
        }

        @Test
        internal fun `will send multiple messages to the event queue`() {
            stubDlqMessageCount(3)
            whenever(eventAwsSqsDlqClient.receiveMessage(any<ReceiveMessageRequest>()))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234AA"))))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234BB"))))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234CC"))))

            queueAdminService.transferEventMessages()

            verify(eventAwsSqsClient).sendMessage(eventQueueUrl, dataComplianceDeleteOffenderMessage("Z1234AA"))
            verify(eventAwsSqsClient).sendMessage(eventQueueUrl, dataComplianceDeleteOffenderMessage("Z1234BB"))
            verify(eventAwsSqsClient).sendMessage(eventQueueUrl, dataComplianceDeleteOffenderMessage("Z1234CC"))
        }

        @Test
        internal fun `will send a telemetry event`() {
            stubDlqMessageCount(1)
            whenever(eventAwsSqsDlqClient.receiveMessage(any<ReceiveMessageRequest>()))
                .thenReturn(ReceiveMessageResult().withMessages(Message().withBody(dataComplianceDeleteOffenderMessage("Z1234AA"))))

            queueAdminService.transferEventMessages()

            verify(telemetryClient).trackEvent("TRANSFERRED_EVENT_DLQ", mapOf("messages-on-queue" to "1"), null)
        }

        @Test
        internal fun `will not send a telemetry event if there are no messages`() {
            stubDlqMessageCount(0)

            queueAdminService.transferEventMessages()

            verifyZeroInteractions(telemetryClient)
        }

        private fun stubDlqMessageCount(count: Int) =
            whenever(eventAwsSqsDlqClient.getQueueAttributes(eventDlqUrl, listOf("ApproximateNumberOfMessages")))
                .thenReturn(GetQueueAttributesResult().withAttributes(mutableMapOf("ApproximateNumberOfMessages" to count.toString())))
    }
}

fun dataComplianceDeleteOffenderMessage(offenderNumber: String) =
    """
    {
  "Type": "Notification",
  "MessageId": "20e13002-d1be-56e7-be8c-66cdd7e23341",
  "TopicArn": "arn:aws:sns:eu-west-2:754256621582:cloud-platform-Digital-Prison-Services-f221e27fcfcf78f6ab4f4c3cc165eee7",
  "Message": "{\"eventType\":\"DATA_COMPLIANCE_DELETE-OFFENDER\",\"eventDatetime\":\"2020-02-25T11:24:32.935401\",\"offenderIdDisplay\":\"$offenderNumber"\",\"nomisEventType\":\"DATA_COMPLIANCE_DELETE-OFFENDER\"}",
  "Timestamp": "2020-02-25T11:25:16.169Z",
  "SignatureVersion": "1",
  "Signature": "h5p3FnnbsSHxj53RFePh8HR40cbVvgEZa6XUVTlYs/yuqfDsi17MPA+bX4ijKmmTT2l6xG2xYhcmRAbJWQ4wrwncTBm2azgiwSO5keRNWYVdiC0rI484KLZboP1SDsE+Y7hOU/R0dz49q7+0yd+QIocPteKB/8xG7/6kjGStAZKf3cEdlxOwLhN+7RU1Yk2ENuwAJjVRtvlAa76yKB3xvL2hId7P7ZLmHGlzZDNZNYxbg9C8HGxteOzZ9ZeeQsWDf9jmZ+5+7dKXQoW9LeqwHxEAq2vuwSZ8uwM5JljXbtS5w1P0psXPYNoin2gU1F5MDK8RPzjUtIvjINx08rmEOA==",
  "SigningCertURL": "https://sns.eu-west-2.amazonaws.com/SimpleNotificationService-a86cb10b4e1f29c941702d737128f7b6.pem",
  "UnsubscribeURL": "https://sns.eu-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-2:754256621582:cloud-platform-Digital-Prison-Services-f221e27fcfcf78f6ab4f4c3cc165eee7:92545cfe-de5d-43e1-8339-c366bf0172aa",
  "MessageAttributes": {
    "eventType": {
      "Type": "String",
      "Value": "DATA_COMPLIANCE_DELETE-OFFENDER"
    },
    "id": {
      "Type": "String",
      "Value": "cb4645f2-d0c1-4677-806a-8036ed54bf69"
    },
    "contentType": {
      "Type": "String",
      "Value": "text/plain;charset=UTF-8"
    },
    "timestamp": {
      "Type": "Number.java.lang.Long",
      "Value": "1582629916147"
    }
  }
}
  """.trimIndent()
