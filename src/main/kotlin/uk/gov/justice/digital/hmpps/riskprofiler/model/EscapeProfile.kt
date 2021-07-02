package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank

data class EscapeProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,
  @field:ApiModelProperty(
    value = "Indicates offender is on the escape list",
    example = "true",
  ) val activeEscapeList: Boolean,

  @field:ApiModelProperty(
    value = "Indicates offender is an escape risk",
    example = "true",
  ) val activeEscapeRisk: Boolean,

  @field:ApiModelProperty(value = "Active escape list alerts") val escapeListAlerts: List<Alert>?,

  @field:ApiModelProperty(value = "Active escape risk alerts") val escapeRiskAlerts: List<Alert>?
) : RiskProfile(nomsId, provisionalCategorisation) {

  constructor() : this("", "", false, false, null, null)

  override fun getRiskType(): RiskType {
    return RiskType.ESCAPE
  }
}
