package uk.gov.justice.digital.hmpps.riskprofiler.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime

@Entity
@Table(name = "PREVIOUS_PROFILE")
data class PreviousProfile(
  @Id
  val offenderNo: @Length(max = 10) String,
  var escape: String,
  var soc: String,
  var violence: String,
  var executeDateTime: @NotNull LocalDateTime,
)
