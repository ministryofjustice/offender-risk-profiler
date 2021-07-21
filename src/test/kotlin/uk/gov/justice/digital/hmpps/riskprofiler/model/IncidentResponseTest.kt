package uk.gov.justice.digital.hmpps.riskprofiler.model

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.serializer.DefaultDeserializer
import org.springframework.core.serializer.DefaultSerializer
import java.time.LocalDateTime
import java.time.Month

@ExtendWith(MockitoExtension::class)
class IncidentResponseTest {

  @Test
  fun testSerialisation() {
    val model = IncidentResponse(2, 3)
    model.question = "hi"
    model.answer = "there"
    model.questionSeq = 34
    model.responseDate = LocalDateTime.of(2021,Month.APRIL,23,13,1)
    model.responseCommentText = "people"
    model.recordStaffId = 5L

    val serialised = DefaultSerializer().serializeToByteArray(model)
    val reConstituted = DefaultDeserializer().deserializeFromByteArray(serialised) as IncidentResponse

    Assertions.assertThat(reConstituted).isEqualTo(model)
    Assertions.assertThat(reConstituted.question).isEqualTo(model.question)
    Assertions.assertThat(reConstituted.answer).isEqualTo(model.answer)
    Assertions.assertThat(reConstituted.questionSeq).isEqualTo(model.questionSeq)
    Assertions.assertThat(reConstituted.responseDate).isEqualTo(model.responseDate)
    Assertions.assertThat(reConstituted.responseCommentText).isEqualTo(model.responseCommentText)
    Assertions.assertThat(reConstituted.recordStaffId).isEqualTo(model.recordStaffId)
  }

  @Test
  fun testSerialisationWithNulls() {
    val model = IncidentResponse(2, 3)

    val serialised = DefaultSerializer().serializeToByteArray(model)
    val reConstituted = DefaultDeserializer().deserializeFromByteArray(serialised) as IncidentResponse

    Assertions.assertThat(reConstituted).isEqualTo(model)
    Assertions.assertThat(reConstituted.question).isEqualTo(model.question)
    Assertions.assertThat(reConstituted.answer).isEqualTo(model.answer)
    Assertions.assertThat(reConstituted.questionSeq).isEqualTo(model.questionSeq)
    Assertions.assertThat(reConstituted.responseDate).isEqualTo(model.responseDate)
    Assertions.assertThat(reConstituted.responseCommentText).isEqualTo(model.responseCommentText)
    Assertions.assertThat(reConstituted.recordStaffId).isEqualTo(model.recordStaffId)
  }
}
