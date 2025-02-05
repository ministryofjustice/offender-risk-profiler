package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Alert(

  val alertCode: String,
  var dateCreated: LocalDate,
  val activeFrom: LocalDate? = null,
  var dateExpires: LocalDate? = null,
  val active: Boolean,
) : Serializable {

  fun isExpired(): Boolean {
    if (this.dateExpires != null) {
      return this.dateExpires!!.isBefore(LocalDate.now())
    }
    return false
  }

  companion object {
    private const val serialVersionUID: Long = 1
  }
}
