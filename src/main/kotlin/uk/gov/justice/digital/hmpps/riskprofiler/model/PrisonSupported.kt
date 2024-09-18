package uk.gov.justice.digital.hmpps.riskprofiler.model

import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "PRISON_SUPPORTED")
data class PrisonSupported(
  @Id
  @Length(max = 6)
  val prisonId: String,

  val startDateTime: @NotNull LocalDateTime? = null,
)
