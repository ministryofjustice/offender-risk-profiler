package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentParty(
  val bookingId: Long? = null,
  val partySeq: Long? = null,
  val staffId: Long? = null,
  val personId: Long? = null,
  val participationRole: String? = null,
  val outcomeCode: String? = null,
  val commentText: String? = null,
  val incidentCaseId: Long? = null,
) : Serializable
