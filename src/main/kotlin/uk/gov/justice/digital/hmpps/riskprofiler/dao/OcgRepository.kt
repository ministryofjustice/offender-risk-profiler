package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg
import java.time.LocalDateTime

@Repository
class OcgRepository : DataRepository<Ocg>() {
  override fun doProcess(
    csvData: List<List<String>>,
    filename: String,
    timestamp: LocalDateTime,
    data: ImportedFile<Ocg>,
  ) {
    data.fileTimestamp = timestamp
    data.fileName = filename
    data.fileType = FileType.OCG
    data.reset()
    csvData.stream().filter { data.index.getAndIncrement() > 0 }
      .forEach { p: List<String> ->
        try {
          val key = p[Ocg.OCG_ID_POSITION]
          if (StringUtils.isNotBlank(key)) {
            if (data.dataSet!![key] != null) {
              log.warn("Duplicate key found in line {} for Key {}", data.index.get(), key)
              data.linesDup.incrementAndGet()
            } else {
              val line = Ocg(key, StringUtils.trimToNull(p[Ocg.OCGM_BAND_POSITION]))
              data.dataSet!![key] = line
              data.linesProcessed.incrementAndGet()
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
    log.info("Lines total ${data.index.get()}, processed ${data.linesProcessed.get()}, dups ${data.linesDup.get()}, invalid ${data.linesInvalid.get()}, errors ${data.linesError.get()}")
  }

  companion object {
    private val log = LoggerFactory.getLogger(OcgRepository::class.java)
  }
}
