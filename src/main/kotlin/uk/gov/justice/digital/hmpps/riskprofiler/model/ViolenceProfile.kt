package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class ViolenceProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,
  @field:Schema(
    title = "Indicates that offender is very high risk of violence",
    example = "false",
  ) val veryHighRiskViolentOffender: Boolean,
  @field:Schema(
    title = "Notify That Safety Custody Lead should be informed",
    example = "true",
  ) var notifySafetyCustodyLead: Boolean,
  @field:Schema(
    title = "Indicates that number of assaults and number serious should be displayed",
    example = "false",
  ) val displayAssaults: Boolean,
  @field:Schema(
    title = "The number of assaults for this offender",
    example = "4",
  ) val numberOfAssaults: Long,
  @field:Schema(
    title = "The number of serious assaults in the last 12 months",
    example = "2",
  ) val numberOfSeriousAssaults: Long,
  @field:Schema(
    title = "The number of non-serious assaults in the last 12 months",
    example = "2",
  ) val numberOfNonSeriousAssaults: Long,
) : RiskProfile(nomsId, provisionalCategorisation) {
  constructor() : this("", "", false, false, false, 0, 0, 0)

  override fun getRiskType(): RiskType {
    return RiskType.VIOLENCE
  }
}
