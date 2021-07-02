package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * A COPY of the custody-api class
 */
data class OffenderEvent(
  var offenderId: Long? = null
) {
  constructor() : this(null)

  private val eventId: String? = null
  val eventType: String? = null
  private val eventDatetime: LocalDateTime? = null
  private val rootOffenderId: Long? = null
  private val aliasOffenderId: Long? = null
  private val previousOffenderId: Long? = null
  private val offenderIdDisplay: String? = null
  val bookingId: Long? = null
  private val bookingNumber: String? = null
  private val previousBookingNumber: String? = null
  private val sanctionSeq: Long? = null
  private val movementSeq: Long? = null
  private val imprisonmentStatusSeq: Long? = null
  private val assessmentSeq: Long? = null
  private val alertSeq: Long? = null
  private val alertDateTime: LocalDateTime? = null
  private val alertType: String? = null
  val alertCode: String? = null
  private val expiryDateTime: LocalDateTime? = null
  private val caseNoteId: Long? = null
  private val agencyLocationId: String? = null
  private val riskPredictorId: Long? = null
  private val addressId: Long? = null
  private val personId: Long? = null
  private val sentenceCalculationId: Long? = null
  private val oicHearingId: Long? = null
  private val oicOffenceId: Long? = null
  private val pleaFindingCode: String? = null
  private val findingCode: String? = null
  private val resultSeq: Long? = null
  private val agencyIncidentId: Long? = null
  private val chargeSeq: Long? = null
  private val identifierType: String? = null
  private val identifierValue: String? = null
  private val ownerId: Long? = null
  private val ownerClass: String? = null
  private val sentenceSeq: Long? = null
  private val conditionCode: String? = null
  private val offenderSentenceConditionId: Long? = null
  private val addressEndDate: LocalDate? = null
  private val primaryAddressFlag: String? = null
  private val mailAddressFlag: String? = null
  private val addressUsage: String? = null

  // incident event data
  val incidentCaseId: Long? = null
  private val incidentPartySeq: Long? = null
  private val incidentRequirementSeq: Long? = null
  private val incidentQuestionSeq: Long? = null
  private val incidentResponseSeq: Long? = null

  // external movement event data
  private val movementDateTime: LocalDateTime? = null
  private val movementType: String? = null
  private val movementReasonCode: String? = null
  private val directionCode: String? = null
  private val escortCode: String? = null
  private val fromAgencyLocationId: String? = null
  private val toAgencyLocationId: String? = null
  private val nomisEventType: String? = null
}
