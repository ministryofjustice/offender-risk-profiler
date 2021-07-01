package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.io.Serializable
import java.time.LocalDate

@ApiModel(description = "Offender Sentence terms details for booking id")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderSentenceTerms(
  @ApiModelProperty(required = true, value = "Offender booking id.", example = "1132400")
  private val bookingId: Long? = null,

  @ApiModelProperty(required = true, value = "Sentence number within booking id.", example = "2")
  private val sentenceSequence: Int? = null,

  @ApiModelProperty(required = true, value = "Sentence term number within sentence.", example = "1")
  private val termSequence: Int? = null,

  @ApiModelProperty(
    value = "Sentence number which this sentence follows if consecutive, otherwise concurrent.",
    example = "2"
  )
  private val consecutiveTo: Int? = null,

  @ApiModelProperty(
    value = "Sentence type, using reference data from table SENTENCE_CALC_TYPES.",
    example = "2"
  )
  private val sentenceType: String? = null,

  @ApiModelProperty(value = "Sentence type description.", example = "2")
  private val sentenceTypeDescription: String? = null,

  @ApiModelProperty(required = true, value = "Start date of sentence.", example = "2018-12-31")
  private val startDate: LocalDate? = null,

  @ApiModelProperty(value = "Sentence length years.")
  private val years: Int? = null,

  @ApiModelProperty(value = "Sentence length months.")
  private val months: Int? = null,

  @ApiModelProperty(value = "Sentence length weeks.")
  private val weeks: Int? = null,

  @ApiModelProperty(value = "Sentence length days.")
  private val days: Int? = null,

  @ApiModelProperty(required = true, value = "Whether this is a life sentence.")
  val lifeSentence: Boolean? = null
) : Serializable {

  constructor(booking: Long, lifeSentence: Boolean) : this(bookingId = booking, lifeSentence = lifeSentence)
}
