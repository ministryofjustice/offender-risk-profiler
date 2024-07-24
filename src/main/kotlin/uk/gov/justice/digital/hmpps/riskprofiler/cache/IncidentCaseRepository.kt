package uk.gov.justice.digital.hmpps.riskprofiler.cache

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase

@Repository
interface IncidentCaseRepository : CrudRepository<IncidentCase?, String?> {
  override fun findAll(): List<IncidentCase>
}
