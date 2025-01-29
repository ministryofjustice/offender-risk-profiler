package uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A summary of the alert")
data class PrisonerAlertCodeSummaryDto(
  @Schema(required = true, description = "Alert Code", example = ALERT_CODE_ESCAPE_RISK)
  val code: String,

  @Schema(required = true, description = "Alert Code Description", example = "Escape Risk")
  val description: String,
) {
  companion object {
    const val ALERT_CODE_ESCAPE_RISK = "XER"
    const val ALERT_CODE_ESCAPE_LIST = "XEL"
  }
}