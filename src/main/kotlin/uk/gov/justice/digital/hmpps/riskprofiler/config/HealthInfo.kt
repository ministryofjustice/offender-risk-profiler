package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet

/**
 * Adds version data to the /health endpoint, and checks all repos have data available.
 * This is called by the UI to display API details
 */
@Component
class HealthInfo : HealthIndicator {
  @Autowired(required = false)
  lateinit var buildProperties: BuildProperties

  @Autowired
  lateinit var dataRepositoryFactory: DataRepositoryFactory

  override fun health(): Health {
    val allAvailable = dataRepositoryFactory.getRepositories()
      .stream().map { obj: DataRepository<out RiskDataSet> -> obj.dataAvailable() }
      .reduce { accumulator: Boolean, dataAvailable: Boolean -> accumulator && dataAvailable }
    return Health.status(
      if (allAvailable.orElse(false)) Status.UP else Status.OUT_OF_SERVICE
    )
      .withDetail("version", version).build()
  }

  val version: String
    get() = if (!this::buildProperties.isInitialized) "version not available" else buildProperties.version
}
