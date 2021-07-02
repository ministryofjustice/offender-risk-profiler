package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.riskprofiler.model.PreviousProfile
import java.time.LocalDateTime

@Repository
interface PreviousProfileRepository : CrudRepository<PreviousProfile, String> {
  @Query("select max(pp.executeDateTime) from PreviousProfile pp")
  fun findApproxLastRunTime(): LocalDateTime?
}
