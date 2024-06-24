package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class EscapeProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,
  @field:Schema(
    title = "Indicates offender is on the escape list",
    example = "true",
  ) val activeEscapeList: Boolean,

  @field:Schema(
    title = "Indicates offender is an escape risk",
    example = "true",
  ) val activeEscapeRisk: Boolean,

  @field:Schema(title = "Active escape risk alerts") val escapeRiskAlerts: List<Alert>?,

  @field:Schema(title = "Active escape list alerts") val escapeListAlerts: List<Alert>?

) : RiskProfile(nomsId, provisionalCategorisation) {

  constructor() : this("", "", false, false, null, null)

  override fun getRiskType(): RiskType {
    return RiskType.ESCAPE
  }
}
