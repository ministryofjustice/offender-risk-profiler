package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "RiskProfile")
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class RiskProfile(
  @Schema(required = true, title = "Identifies the offender by NOMS ID.", example = "ZWE123A")
  private var nomsId: @NotBlank String = "",

  @Schema(required = true, title = "Provisional Categorisation", example = "C")
  private var provisionalCategorisation: @NotBlank String = "C"
) {
  @Schema(
    required = true,
    title = "Risk Type, VIOLENCE, SOC, EXTREMISM, ESCAPE, LIFE;",
    example = "EXTREMISM",
  )
  abstract fun getRiskType(): RiskType

  companion object {
    const val DEFAULT_CAT = "C"
  }
}
