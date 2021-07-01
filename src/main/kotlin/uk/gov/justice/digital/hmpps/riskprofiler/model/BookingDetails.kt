package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BookingDetails(val bookingId: Long? = null) : Serializable {
  var offenderNo: String? = null

  companion object {
    private const val serialVersionUID = 2L
  }
}
