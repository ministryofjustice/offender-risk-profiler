package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModelProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
  @ApiModelProperty(required = true, value = "Status of Error Code", example = "400")
  val status: Int,

  @ApiModelProperty(required = false, value = "Develop Information message", example = "System is down")
  val developerMessage: String? = null
) {
  @ApiModelProperty(required = true, value = "Internal Error Code", example = "20012")
  private val errorCode: Int? = null

  @ApiModelProperty(required = true, value = "Error message information", example = "Offender Not Found")
  private val userMessage: String? = null

  @ApiModelProperty(
    required = false,
    value = "Additional information about the error",
    example = "Hard disk failure"
  )
  private val moreInfo: String? = null
}
