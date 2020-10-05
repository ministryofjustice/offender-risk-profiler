package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriTemplate;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PathfinderService {
    private final OAuth2RestTemplate restTemplate;

    @Autowired
    public PathfinderService(@Qualifier("pathfinderSystemRestTemplate") final OAuth2RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> T get(final URI uri, final Class<T> responseType) {
        final ResponseEntity<T> exchange = restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                new HttpEntity<>(null, RestCallHelper.CONTENT_TYPE_APPLICATION_JSON),
                responseType);
        return exchange.getBody();
    }

    public Optional<PathFinder> getBand(final String nomsId) {

        log.debug("Getting noms id {} from pathfinder api", nomsId);
        final var uri = new UriTemplate("/pathfinder/offender/{nomsId}").expand(nomsId);
        try {
            final var map = get(uri, Map.class);

            return Optional.of(PathFinder.builder()
                    .nomisId((String) map.get("nomsId"))
                    .pathFinderBanding((Integer) map.get("band"))
                    .build());

        } catch (final HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
