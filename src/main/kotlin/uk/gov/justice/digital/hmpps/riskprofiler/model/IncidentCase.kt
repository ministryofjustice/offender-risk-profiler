package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentCase(val incidentCaseId: Long? = null) : Serializable {

  constructor(incidentStatus: String, reportTime: LocalDateTime, responses: List<IncidentResponse>) : this(0) {
    this.incidentStatus = incidentStatus
    this.reportTime = reportTime
    this.responses = responses
  }

  val incidentTitle: String? = null
  var incidentType: String? = null
  val incidentDetails: String? = null
  val incidentDate: LocalDate? = null
  val incidentTime: LocalDateTime? = null
  val reportedStaffId: Long? = null
  val reportDate: LocalDate? = null
  var reportTime: LocalDateTime? = null
  var incidentStatus: String? = null
  val agencyId: String? = null
  val responseLockedFlag: Boolean? = null
  var responses: List<IncidentResponse>? = null
  var parties: List<IncidentParty>? = null
}
