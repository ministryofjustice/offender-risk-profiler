package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.apache.camel.ExchangeProperty
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet
import java.time.LocalDateTime

@Service
class DataService(private val factory: DataRepositoryFactory) {
  fun process(
    csvData: List<List<String>>,
    @ExchangeProperty("fileType") fileType: FileType,
    @ExchangeProperty("fileInfo") fileInfo: PendingFile,
  ) {
    val repository = factory.getRepository(fileType.type)
    if (isFileShouldBeProcessed(repository, fileInfo.fileTimestamp)) {
      repository.process(csvData, fileInfo.fileName!!, fileInfo.fileTimestamp!!)
      log.info("Processed {}", fileInfo.fileName)
    } else {
      log.warn("Skipped {}", fileInfo.fileName)
    }
  }

  private fun isFileShouldBeProcessed(data: DataRepository<out RiskDataSet?>, timestamp: LocalDateTime?): Boolean {
    return data.fileTimestamp == null || data.fileTimestamp!! < timestamp
  }

  companion object {
    private val log = LoggerFactory.getLogger(DataService::class.java)
  }
}
