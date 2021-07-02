package uk.gov.justice.digital.hmpps.riskprofiler.dao

import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

data class ImportedFile<RiskDataSet>(
  var fileName: String? = null,
  var fileTimestamp: LocalDateTime? = null,
  var dataSet: MutableMap<String, RiskDataSet>? = null,
  var fileType: FileType? = null,
  val index: AtomicInteger = AtomicInteger(),
  val linesProcessed: AtomicInteger = AtomicInteger(),
  val linesDup: AtomicInteger = AtomicInteger(),
  val linesError: AtomicInteger = AtomicInteger(),
  val linesInvalid: AtomicInteger = AtomicInteger(),
) {
  fun reset() {
    dataSet = HashMap()
    index.set(0)
    linesProcessed.set(0)
    linesDup.set(0)
    linesError.set(0)
    linesInvalid.set(0)
  }
}
