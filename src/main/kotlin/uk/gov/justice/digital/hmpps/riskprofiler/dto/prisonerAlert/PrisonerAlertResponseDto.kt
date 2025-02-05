package uk.gov.justice.digital.hmpps.riskprofiler.dto.prisonerAlert

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Alert dto response from alerts API")
data class PrisonerAlertResponseDto(
  @Schema(description = "A summary of the alert", example = "2020-08-20", required = true)
  val alertCode: PrisonerAlertCodeSummaryDto,

  @Schema(description = "Date the alert was created", required = true, example = "2019-08-20")
  val createdAt: LocalDate,

  @Schema(description = "Date the alert expires", example = "2020-08-20")
  val activeTo: LocalDate? = null,

  @Schema(description = "Date the alert became active, which might differ to the date it was created", required = true, example = "2020-08-20")
  val activeFrom: LocalDate,

  @Schema(description = "True / False based on alert status", example = "false", required = true)
  @JsonProperty("isActive")
  val active: Boolean,
)