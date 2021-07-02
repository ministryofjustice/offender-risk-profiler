package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank

@ApiModel(description = "RiskProfile")
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class RiskProfile(
  @ApiModelProperty(required = true, value = "Identifies the offender by NOMS ID.", example = "ZWE123A")
  private var nomsId: @NotBlank String = "",

  @ApiModelProperty(required = true, value = "Provisional Categorisation", example = "C")
  private var provisionalCategorisation: @NotBlank String = "C"

) {

  @ApiModelProperty(
    required = true,
    value = "Risk Type, VIOLENCE, SOC, EXTREMISM, ESCAPE;",
    example = "EXTREMISM",
    position = 1
  )
  abstract fun getRiskType(): RiskType

  companion object {
    const val DEFAULT_CAT = "C"
  }
}
