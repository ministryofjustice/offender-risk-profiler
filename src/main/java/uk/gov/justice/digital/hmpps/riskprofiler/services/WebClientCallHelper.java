package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PagingAndSortingDto;

/**
 * Helper class that takes care of setting up rest template with base API url and request headers.
 */
@Component
public class WebClientCallHelper {

    public static final HttpHeaders CONTENT_TYPE_APPLICATION_JSON = httpContentTypeHeaders();

    private final WebClient webClient;

    @Autowired
    public WebClientCallHelper(@Qualifier("elite2SystemWebClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T> ResponseEntity<T> getForList(final String uri, final ParameterizedTypeReference<T> responseType) {
        return getForList(uri, responseType, null);
    }

    protected <T> ResponseEntity<T> getForList(final String uri, final ParameterizedTypeReference<T> responseType, final HttpHeaders headers) {
        return webClient
                .get()
                .uri(uri)
                .headers(h -> {
                    if (headers != null) {
                        h.addAll(headers);
                    }
                })
                .retrieve()
                .toEntity(responseType)
                .block();
    }

    protected <T> T get(final String uri, final Class<T> responseType) {
        return webClient
                .get()
                .uri(uri)
                .headers(h -> {
                    h.addAll(CONTENT_TYPE_APPLICATION_JSON);
                })
                .retrieve()
                .toEntity(responseType)
                .block()
                .getBody();
    }

    protected <T> ResponseEntity<T> getWithPaging(final String uri, final PagingAndSortingDto pagingAndSorting,
                                                  final ParameterizedTypeReference<T> responseType) {
        return webClient.get()
                .uri(uri)
                .header(PagingAndSortingDto.HEADER_PAGE_OFFSET, pagingAndSorting.getPageOffset().toString())
                .header(PagingAndSortingDto.HEADER_PAGE_LIMIT, pagingAndSorting.getPageLimit().toString())
                .retrieve()
                .toEntity(responseType)
                .block();
    }

    private static HttpHeaders httpContentTypeHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }
}
