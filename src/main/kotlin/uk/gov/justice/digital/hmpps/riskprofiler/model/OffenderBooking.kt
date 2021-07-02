package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import java.io.Serializable

@ApiModel(description = "Offender Booking Summary")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderBooking(
  val bookingId: Long? = null,
  val bookingNo: String? = null,
  val offenderNo: String? = null,
  val imprisonmentStatus: String? = null,
) : Serializable
