package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
class IncidentResponse(
  val questionnaireQueId: Long,
  val questionnaireAnsId: Long
) : Serializable {

  constructor(question: String, answer: String) : this(0, 0) {
    this.question = question
    this.answer = answer
  }

  var question: String? = null
  var answer: String? = null
  val questionSeq = 0
  val responseDate: LocalDateTime? = null
  val responseCommentText: String? = null
  val recordStaffId: Long? = null
}
