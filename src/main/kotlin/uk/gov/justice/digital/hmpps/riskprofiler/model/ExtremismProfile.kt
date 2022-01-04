package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

data class ExtremismProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,
  @field:Schema(title = "Indicates Regional CT Lead should be informed", example = "false")
  var notifyRegionalCTLead: Boolean = false,
  @field:Schema(
    title = "Indicates that there is data to indicate that this person has an increased risk of engaging in extremism",
    example = "false"
  )
  var increasedRiskOfExtremism: Boolean = false
) : RiskProfile(nomsId, provisionalCategorisation) {

  constructor() : this("", "", false, false)

  override fun getRiskType(): RiskType {
    return RiskType.EXTREMISM
  }
}
