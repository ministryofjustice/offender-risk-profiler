package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import java.time.LocalDate

@ApiModel(description = "Assault")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Assault(
  private val offenderNo: String? = null,
  private val type: String? = null,
  private val dateCreated: LocalDate? = null,
  private val serious: Boolean = false,
)
