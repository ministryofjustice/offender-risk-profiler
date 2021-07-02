package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import java.io.Serializable
import java.time.LocalDate

@ApiModel(description = "Alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Alert(

  private val alertId: Long? = null,
  private val bookingId: Long? = null,
  private val offenderNo: String? = null,
  private val alertType: String? = null,
  private val alertTypeDescription: String? = null,
  val alertCode: String? = null,
  private val alertCodeDescription: String? = null,
  private val comment: String? = null,
  var dateCreated: LocalDate? = null,
  var dateExpires: LocalDate? = null,
  var expired: Boolean? = false,
  val active: Boolean? = false,
  private val addedByFirstName: String? = null,
  private val addedByLastName: String? = null,
  private val expiredByFirstName: String? = null,
  private val expiredByLastName: String? = null,
  private val ranking: Int? = 0,
) : Serializable {

  constructor(active: Boolean, code: String) : this(active = active, alertCode = code)
}
