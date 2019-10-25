package uk.gov.justice.digital.hmpps.riskprofiler.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PagingAndSortingDto;

import java.net.URI;

/**
 * Helper class that takes care of setting up rest template with base API url and request headers.
 */
@Component
public class RestCallHelper {

    private static final HttpHeaders CONTENT_TYPE_APPLICATION_JSON = httpContentTypeHeaders();

    private final OAuth2RestTemplate restTemplate;

    @Autowired
    public RestCallHelper(OAuth2RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> ResponseEntity<T> getForList(URI uri, ParameterizedTypeReference<T> responseType) {
       return getForList(uri, responseType, null);
    }

    protected <T> ResponseEntity<T> getForList(URI uri, ParameterizedTypeReference<T> responseType, HttpHeaders headers) {
        return restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                headers == null ? null : new HttpEntity<>(null, headers),
                responseType);
    }

    protected <T> T get(URI uri, Class<T> responseType) {
        ResponseEntity<T> exchange = restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                new HttpEntity<>(null, CONTENT_TYPE_APPLICATION_JSON),
                responseType);
        return exchange.getBody();
    }

    protected <T> ResponseEntity<T> getWithPaging(final URI uri, final PagingAndSortingDto pagingAndSorting,
                                                  final ParameterizedTypeReference<T> responseType) {
        return restTemplate.exchange(
                uri.toString(),
                HttpMethod.GET,
                withPaging(pagingAndSorting),
                responseType);
    }

    private HttpEntity<?> withPaging(final PagingAndSortingDto pagingAndSorting) {
        final var headers = new HttpHeaders();

        headers.add(PagingAndSortingDto.HEADER_PAGE_OFFSET, pagingAndSorting.getPageOffset().toString());
        headers.add(PagingAndSortingDto.HEADER_PAGE_LIMIT, pagingAndSorting.getPageLimit().toString());

        return new HttpEntity<>(null, headers);
    }

    private static HttpHeaders httpContentTypeHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }
}
