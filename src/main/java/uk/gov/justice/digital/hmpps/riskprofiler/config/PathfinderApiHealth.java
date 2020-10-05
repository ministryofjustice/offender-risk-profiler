package uk.gov.justice.digital.hmpps.riskprofiler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class PathfinderApiHealth implements HealthIndicator {

    private final RestTemplate restTemplate;

    @Autowired
    public PathfinderApiHealth(@Qualifier("pathfinderApiHealthRestTemplate") final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity("/ping", String.class);
            return health(Health.up(), responseEntity.getStatusCode());
        } catch (final RestClientException e) {
            return health(Health.outOfService(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private Health health(final Health.Builder builder, final HttpStatus code) {
        return builder
                .withDetail("HttpStatus", code.value())
                .build();
    }
}
