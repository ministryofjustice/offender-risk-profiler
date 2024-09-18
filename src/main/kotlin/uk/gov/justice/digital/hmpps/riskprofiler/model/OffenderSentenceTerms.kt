package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable
import java.time.LocalDate

@Schema(title = "Offender Sentence terms details for booking id")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderSentenceTerms(
  @Schema(required = true, title = "Offender booking id.", example = "1132400")
  private val bookingId: Long? = null,

  @Schema(required = true, title = "Sentence number within booking id.", example = "2")
  private val sentenceSequence: Int? = null,

  @Schema(required = true, title = "Sentence term number within sentence.", example = "1")
  private val termSequence: Int? = null,

  @Schema(
    title = "Sentence number which this sentence follows if consecutive, otherwise concurrent.",
    example = "2",
  )
  private val consecutiveTo: Int? = null,

  @Schema(
    title = "Sentence type, using reference data from table SENTENCE_CALC_TYPES.",
    example = "2",
  )
  private val sentenceType: String? = null,

  @Schema(title = "Sentence type description.", example = "2")
  private val sentenceTypeDescription: String? = null,

  @Schema(required = true, title = "Start date of sentence.", example = "2018-12-31")
  private val startDate: LocalDate? = null,

  @Schema(title = "Sentence length years.")
  private val years: Int? = null,

  @Schema(title = "Sentence length months.")
  private val months: Int? = null,

  @Schema(title = "Sentence length weeks.")
  private val weeks: Int? = null,

  @Schema(title = "Sentence length days.")
  private val days: Int? = null,

  @Schema(required = true, title = "Whether this is a life sentence.")
  val lifeSentence: Boolean? = null,
) : Serializable {

  constructor(booking: Long, lifeSentence: Boolean) : this(bookingId = booking, lifeSentence = lifeSentence)
}
