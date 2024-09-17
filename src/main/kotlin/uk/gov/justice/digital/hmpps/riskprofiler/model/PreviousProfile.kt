package uk.gov.justice.digital.hmpps.riskprofiler.model

import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "PREVIOUS_PROFILE")
data class PreviousProfile(
  @Id
  @Length(max = 10)
  val offenderNo: String,

  var escape: String,
  var soc: String,
  var violence: String,
  var executeDateTime: @NotNull LocalDateTime,
)
