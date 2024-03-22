package uk.gov.justice.digital.hmpps.riskprofiler.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime

@Entity
@Table(name = "PRISON_SUPPORTED")
data class PrisonSupported(
  @Id
  @Length(max = 6)
  val prisonId: String,

  val startDateTime: @NotNull LocalDateTime? = null,
)
