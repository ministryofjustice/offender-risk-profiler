package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Error")
data class ErrorResponse(
  @Schema(required = true, title = "Status of Error Code", example = "400")
  val status: Int,

  @Schema(required = false, title = "Develop Information message", example = "System is down")
  val developerMessage: String? = null,
) {
  @Schema(required = true, title = "Internal Error Code", example = "20012")
  private val errorCode: Int? = null

  @Schema(required = true, title = "Error message information", example = "Offender Not Found")
  private val userMessage: String? = null

  @Schema(
    required = false,
    title = "Additional information about the error",
    example = "Hard disk failure",
  )
  private val moreInfo: String? = null
}
