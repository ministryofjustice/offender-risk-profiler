package uk.gov.justice.digital.hmpps.riskprofiler.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PagingAndSortingDto(
  val pageOffset: Long? = null,
  val pageLimit: Long? = null,
) {
  constructor() : this(null, null)

  companion object {
    const val HEADER_PAGE_OFFSET = "Page-Offset"
    const val HEADER_PAGE_LIMIT = "Page-Limit"
    const val HEADER_TOTAL_RECORDS = "Total-Records"
    const val HEADER_SORT_FIELDS = "Sort-Fields"
    const val HEADER_SORT_ORDER = "Sort-Order"
  }
}
