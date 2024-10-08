package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class RestResponsePage<T> : PageImpl<T> {
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  constructor(
    @JsonProperty("content") content: List<T>,
    @JsonProperty("number") number: Int,
    @JsonProperty("size") size: Int,
    @JsonProperty("totalElements") totalElements: Long?,
    @Suppress("UNUSED_PARAMETER")
    @JsonProperty(
      "pageable",
    )
    pageable: JsonNode?,
  ) : super(content, PageRequest.of(number, size), totalElements!!)
  constructor(content: List<T>) : super(content)
}
