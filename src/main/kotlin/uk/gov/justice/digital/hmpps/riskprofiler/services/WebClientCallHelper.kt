package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.riskprofiler.model.PagingAndSortingDto

/**
 * Helper class that takes care of setting up rest template with base API url and request headers.
 */
@Component
class WebClientCallHelper @Autowired constructor(@param:Qualifier("elite2SystemWebClient") private val webClient: WebClient) {
  fun <T> getForList(uri: String, responseType: ParameterizedTypeReference<T>): ResponseEntity<T> {
    return getForList(uri, responseType, null)
  }

  protected fun <T> getForList(
    uri: String,
    responseType: ParameterizedTypeReference<T>,
    headers: HttpHeaders?
  ): ResponseEntity<T> {
    return webClient
      .get()
      .uri(uri)
      .headers { h: HttpHeaders ->
        if (headers != null) {
          h.addAll(headers)
        }
      }
      .retrieve()
      .toEntity(responseType)
      .block() as ResponseEntity<T>
  }

  operator fun <T> get(uri: String, responseType: Class<T>): T {
    return webClient
      .get()
      .uri(uri)
      .headers { h: HttpHeaders -> h.addAll(CONTENT_TYPE_APPLICATION_JSON) }
      .retrieve()
      .toEntity(responseType)
      .block()!!.body!!
  }

  fun <T> getWithPaging(
    uri: String,
    pagingAndSorting: PagingAndSortingDto,
    responseType: ParameterizedTypeReference<T>
  ): ResponseEntity<T> {
    return webClient.get()
      .uri(uri)
      .header(PagingAndSortingDto.HEADER_PAGE_OFFSET, pagingAndSorting.pageOffset.toString())
      .header(PagingAndSortingDto.HEADER_PAGE_LIMIT, pagingAndSorting.pageLimit.toString())
      .retrieve()
      .toEntity(responseType)
      .block() as ResponseEntity<T>
  }

  companion object {
    val CONTENT_TYPE_APPLICATION_JSON = httpContentTypeHeaders()
    private fun httpContentTypeHeaders(): HttpHeaders {
      val httpHeaders = HttpHeaders()
      httpHeaders.contentType = MediaType.APPLICATION_JSON
      return httpHeaders
    }
  }
}
