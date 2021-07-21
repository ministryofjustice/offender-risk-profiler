package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentResponse(
  var questionnaireQueId: Long,
  var questionnaireAnsId: Long
) : Serializable {

  constructor(question: String, answer: String) : this(0, 0) {
    this.question = question
    this.answer = answer
  }

  var question: String? = null
  var answer: String? = null
  var questionSeq = 0
  var responseDate: LocalDateTime? = null
  var responseCommentText: String? = null
  var recordStaffId: Long? = null

  // TODO: this custom deserialiser can be removed after 30 days in production,
  // as the old java-based cache entries will have expired, see timeout-days in application.yml
  @Throws(IOException::class, ClassNotFoundException::class)
  private fun readObject(`in`: ObjectInputStream) {
    val f = `in`.readFields()
    questionnaireQueId = f.get("questionnaireQueId", 0L)
    questionnaireAnsId = f.get("questionnaireAnsId", 0L)
    question = f.get("question", null) as String?
    answer = f.get("answer", null) as String?
    questionSeq = f.get("questionSeq", 0)
    responseDate = f.get("responseDate", null) as LocalDateTime?
    responseCommentText = f.get("responseCommentText", null) as String?
    recordStaffId = f.get("recordStaffId", null) as Long?
  }

  companion object {
    private const val serialVersionUID: Long = 1
  }
}
