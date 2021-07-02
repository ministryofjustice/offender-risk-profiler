package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported

@Repository
interface PrisonSupportedRepository : CrudRepository<PrisonSupported?, String?> {
  override fun findAll(): List<PrisonSupported>
}
