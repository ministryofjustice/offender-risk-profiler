package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank

data class LifeProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,
  @field:ApiModelProperty(
    value = "Indicates offender has a court-issued life sentence",
    example = "true"
  )
  val life: Boolean
) : RiskProfile(nomsId, provisionalCategorisation) {
  override fun getRiskType(): RiskType {
    return RiskType.LIFE
  }
}
