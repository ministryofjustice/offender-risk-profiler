package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.CompareToBuilder
import javax.validation.constraints.NotBlank

data class SocProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,

  @field:Schema(
    title = "Indicates the offender must be transferred to security",
    example = "true",
  )
  var transferToSecurity: Boolean,
) : RiskProfile(nomsId, provisionalCategorisation), Comparable<SocProfile> {

  constructor() : this("", "", false)

  override fun getRiskType(): RiskType {
    return RiskType.SOC
  }

  override fun compareTo(other: SocProfile): Int {
    return CompareToBuilder()
      .append(getRiskType(), other.getRiskType())
      .append(this.nomsId, other.nomsId)
      .append(other.transferToSecurity, this.transferToSecurity)
      .append(this.provisionalCategorisation, other.provisionalCategorisation)
      .toComparison()
  }
}
