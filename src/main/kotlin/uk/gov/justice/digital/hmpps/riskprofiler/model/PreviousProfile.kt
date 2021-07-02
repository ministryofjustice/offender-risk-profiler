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
  val offenderNo: @Length(max = 10) String,

  var escape: String? = null,
  // val extremism: String? = null,
  var soc: String? = null,
  var violence: String? = null,
  var executeDateTime: @NotNull LocalDateTime? = null,
)
