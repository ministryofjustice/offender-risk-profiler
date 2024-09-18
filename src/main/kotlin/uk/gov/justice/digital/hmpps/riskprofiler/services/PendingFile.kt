package uk.gov.justice.digital.hmpps.riskprofiler.services

import java.time.LocalDateTime

data class PendingFile(
  val fileName: String? = null,
  val fileTimestamp: LocalDateTime? = null,
  val data: ByteArray,
)
