package uk.gov.justice.digital.hmpps.riskprofiler.integration

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.MediaType
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.StartMessageMoveTaskRequest
import uk.gov.justice.digital.hmpps.riskprofiler.events.EventType
import uk.gov.justice.digital.hmpps.riskprofiler.events.HmppsEvent
import uk.gov.justice.digital.hmpps.riskprofiler.events.Message
import uk.gov.justice.digital.hmpps.riskprofiler.events.MessageAttributes
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue

class HmppsQueueSpyBeanTest : IntegrationTestBase() {

  @Test
  fun `Can verify usage of spy bean for Health page`() {
    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")

    verify(riskProfilerChangeSqsClientSpy).getQueueAttributes(any<GetQueueAttributesRequest>())
  }

  @Test
  fun `Can verify usage of spy bean for retry-dlq endpoint`() {
    val event = HmppsEvent("id", "test.type", "message1")
    val message = Message(gsonString(event), "message-id", MessageAttributes(EventType("test.type", "String")))
    val messageAttributes = mutableMapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("test value").build())
    riskProfilerChangeSqsDlqClientSpy.sendMessage(SendMessageRequest.builder().queueUrl(riskProfilerChangeDlqUrl).messageBody(gsonString(message)).messageAttributes(messageAttributes).build())
    await untilCallTo { riskProfilerChangeSqsDlqClientSpy.countMessagesOnQueue(riskProfilerChangeDlqUrl).get() } matches { it == 1 }

    webTestClient.put()
      .uri("/queue-admin/retry-dlq/${hmppsSqsPropertiesSpy.riskProfilerChangeQueueConfig().dlqName}")
      .headers { it.authToken("API_TEST_USER") }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk

    await untilCallTo { riskProfilerChangeSqsDlqClientSpy.countMessagesOnQueue(riskProfilerChangeDlqUrl).get() } matches { it == 0 }
    await untilCallTo { riskProfilerChangeSqsClientSpy.countMessagesOnQueue(riskProfilerChangeQueueUrl).get() } matches { it == 1 }


    // todo
    //verify(riskProfilerChangeSqsClientSpy).handleMessage(event)

    val captor = argumentCaptor<SendMessageRequest>()
    verify(riskProfilerChangeSqsDlqClientSpy).sendMessage(captor.capture())

    // todo
    // assertThat(captor.firstValue.()).contains("000000000000")
    // assertThat(captor.firstValue.destinationArn()).contains("000000000000")
  }

  @Test
  fun `Can verify usage of spy bean for purge-queue endpoint`() {
    riskProfilerChangeSqsDlqClientSpy.sendMessage(SendMessageRequest.builder().queueUrl(riskProfilerChangeDlqUrl).messageBody(gsonString(HmppsEvent("id", "test.type", "message1"))).build())
    await untilCallTo { riskProfilerChangeSqsDlqClientSpy.countMessagesOnQueue(riskProfilerChangeDlqUrl).get() } matches { it == 1 }

    webTestClient.put()
      .uri("/queue-admin/purge-queue/${hmppsSqsPropertiesSpy.riskProfilerChangeQueueConfig().dlqName}")
      .headers { it.authToken("API_TEST_USER") }
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk

    await untilCallTo { riskProfilerChangeSqsDlqClientSpy.countMessagesOnQueue(riskProfilerChangeDlqUrl).get() } matches { it == 0 }

    // One of these was in the @BeforeEach!
    verify(riskProfilerChangeSqsDlqClientSpy, times(2)).purgeQueue(any<PurgeQueueRequest>())
  }
}
