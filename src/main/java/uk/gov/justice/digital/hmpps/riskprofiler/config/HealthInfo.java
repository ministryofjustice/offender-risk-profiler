package uk.gov.justice.digital.hmpps.riskprofiler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory;

/**
 * Adds version data to the /health endpoint, and checks all repos have data available.
 * This is called by the UI to display API details
 */
@Component
public class HealthInfo implements HealthIndicator {

    @Autowired(required = false)
    private BuildProperties buildProperties;
    @Autowired
    private DataRepositoryFactory dataRepositoryFactory;

    @Override
    public Health health() {
        var allAvailable = dataRepositoryFactory.getRepositories()
                .stream().map(DataRepository::dataAvailable).reduce((accumulator, dataAvailable) -> accumulator && dataAvailable);
        return Health.status(
                allAvailable.orElse(false) ? Status.UP : Status.OUT_OF_SERVICE)
                .withDetail("version", getVersion()).build();
    }

    private String getVersion() {
        return buildProperties == null ? "version not available" : buildProperties.getVersion();
    }
}





