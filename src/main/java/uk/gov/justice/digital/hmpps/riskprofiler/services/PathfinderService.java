package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PathfinderService {
    private final WebClient webClient;

    @Autowired
    public PathfinderService(@Qualifier("pathfinderSystemWebClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T> T get(final String uri, final Class<T> responseType) {
        return webClient.get().uri(uri).retrieve().toEntity(responseType).block().getBody();
    }

    public Optional<PathFinder> getBand(final String nomsId) {

        log.debug("Getting noms id {} from pathfinder api", nomsId);
        final var uri = String.format("/pathfinder/offender/%s", nomsId);
        try {
            final var map = get(uri, Map.class);

            return Optional.of(PathFinder.builder()
                .nomisId((String) map.get("nomsId"))
                .pathFinderBanding((Integer) map.get("band"))
                .build());

        } catch (final WebClientResponseException.NotFound e) {
            return Optional.empty();
        }
    }
}
