package uk.gov.justice.digital.hmpps.riskprofiler.model

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.CompareToBuilder
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class SocProfile(
  val nomsId: @NotBlank String,
  var provisionalCategorisation: @NotBlank String,

  @field:Schema(
    title = "Indicates the offender must be transferred to security",
    example = "true",
  )
  var transferToSecurity: Boolean
) : RiskProfile(nomsId, provisionalCategorisation), Comparable<SocProfile?> {

  constructor() : this("", "", false)

  override fun getRiskType(): RiskType {
    return RiskType.SOC
  }

  override fun compareTo(socProfile: @NotNull SocProfile?): Int {
    return CompareToBuilder()
      .append(getRiskType(), socProfile!!.getRiskType())
      .append(this.nomsId, socProfile.nomsId)
      .append(socProfile.transferToSecurity, this.transferToSecurity)
      .append(this.provisionalCategorisation, socProfile.provisionalCategorisation)
      .toComparison()
  }
}
