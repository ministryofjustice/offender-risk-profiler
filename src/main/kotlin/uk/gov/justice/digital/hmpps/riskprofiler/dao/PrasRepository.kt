package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras
import java.time.LocalDateTime

@Repository
class PrasRepository : DataRepository<Pras>() {
  override fun doProcess(
    csvData: List<List<String>>,
    filename: String,
    timestamp: LocalDateTime,
    data: ImportedFile<Pras>
  ) {
    data.fileTimestamp = timestamp
    data.fileName = filename
    data.fileType = FileType.PRAS
    data.reset()
    csvData.stream().filter { p: List<String>? -> data.index.getAndIncrement() > 0 }
      .forEach { p: List<String> ->
        try {
          val key = p[Pras.NOMIS_ID_POSITION]
          if (StringUtils.isNotBlank(key)) {
            if (!NOMS_ID_REGEX.matcher(key).matches()) {
              log.warn("Invalid Key in line {} for Key {}", data.index.get(), key)
              data.linesInvalid.incrementAndGet()
            } else {
              if (data.dataSet!![key] != null) {
                log.warn("Duplicate key found in line {} for Key {}", data.index.get(), key)
                data.linesDup.incrementAndGet()
              } else {
                val line = Pras(key)
                data.dataSet!![key] = line
                data.linesProcessed.incrementAndGet()
              }
            }
          } else {
            log.warn("Missing Key in line {} key [{}]", data.index.get(), key)
            data.linesInvalid.incrementAndGet()
          }
        } catch (e: Exception) {
          log.warn("Error in Line {} data [{}]", data.index, p)
          data.linesError.incrementAndGet()
        }
      }
    log.info(
      "Lines total {}, processed {}, dups {}, invalid {}, errors {}", data.index.get(),
      data.linesProcessed.get(), data.linesDup.get(), data.linesInvalid.get(), data.linesError.get()
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(PrasRepository::class.java)
  }
}
