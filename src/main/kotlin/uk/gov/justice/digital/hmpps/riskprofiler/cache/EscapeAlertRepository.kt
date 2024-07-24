package uk.gov.justice.digital.hmpps.riskprofiler.cache

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase

@Repository
interface EscapeAlertRepository : CrudRepository<Alert?, String?> {
  override fun findAll(): List<Alert>
}
