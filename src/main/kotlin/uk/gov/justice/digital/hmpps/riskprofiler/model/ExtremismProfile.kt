package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank

data class ExtremismProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,
  @field:ApiModelProperty(value = "Indicates Regional CT Lead should be informed", example = "false")
  var notifyRegionalCTLead: Boolean = false,
  @field:ApiModelProperty(
    value = "Indicates that there is data to indicate that this person has an increased risk of engaging in extremism",
    example = "false"
  )
  var increasedRiskOfExtremism: Boolean = false
) : RiskProfile(nomsId, provisionalCategorisation) {

  constructor() : this("", "", false, false)

  override fun getRiskType(): RiskType {
    return RiskType.EXTREMISM
  }
}
