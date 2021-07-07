package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RiskProfileChange(
  val oldProfile: ProfileMessagePayload,
  val newProfile: ProfileMessagePayload,
  val offenderNo: String,
  val executeDateTime: LocalDateTime,
)
