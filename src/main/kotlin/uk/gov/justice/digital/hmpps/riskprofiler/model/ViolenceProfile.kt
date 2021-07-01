package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank

data class ViolenceProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,
  @field:ApiModelProperty(
    value = "Indicates that offender is very high risk of violence",
    example = "false",
    position = 3
  ) val veryHighRiskViolentOffender: Boolean,
  @field:ApiModelProperty(
    value = "Notify That Safety Custody Lead should be informed",
    example = "true",
    position = 4
  ) var notifySafetyCustodyLead: Boolean,
  @field:ApiModelProperty(
    value = "Indicates that number of assaults and number serious should be displayed",
    example = "false",
    position = 5
  ) val displayAssaults: Boolean,
  @field:ApiModelProperty(
    value = "The number of assaults for this offender",
    example = "4",
    position = 6
  ) val numberOfAssaults: Long,
  @field:ApiModelProperty(
    value = "The number of serious assaults in the last 12 months",
    example = "2",
    position = 7
  ) val numberOfSeriousAssaults: Long,
  @field:ApiModelProperty(
    value = "The number of non-serious assaults in the last 12 months",
    example = "2",
    position = 8
  ) val numberOfNonSeriousAssaults: Long
) : RiskProfile(nomsId, provisionalCategorisation) {
  constructor() : this("", "", false, false, false, 0, 0, 0)

  override fun getRiskType(): RiskType {
    return RiskType.VIOLENCE
  }
}
