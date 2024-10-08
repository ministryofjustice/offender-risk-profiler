package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList
import java.time.LocalDateTime

@Repository
class OcgmRepository : DataRepository<OcgmList>() {
  override fun doProcess(
    csvData: List<List<String>>,
    filename: String,
    timestamp: LocalDateTime,
    data: ImportedFile<OcgmList>,
  ) {
    data.fileTimestamp = timestamp
    data.fileName = filename
    data.fileType = FileType.OCGM
    data.reset()
    csvData.stream().filter { data.index.getAndIncrement() > 0 }
      .forEach { p ->
        try {
          val key = p[Ocgm.NOMIS_ID_POSITION]
          if (StringUtils.isNotBlank(key)) {
            if (!NOMS_ID_REGEX.matcher(key).matches()) {
              log.warn("Invalid Key in line {} for Key {}", data.index.get(), key)
              data.linesInvalid.incrementAndGet()
            } else {
              val ocgId = p[Ocgm.OCG_ID_POSITION]
              if (StringUtils.isBlank(ocgId)) {
                log.warn("No OCG Id in line {} for Key {}", data.index.get(), key)
                data.linesInvalid.incrementAndGet()
              } else {
                val ocgmLine =
                  Ocgm(key, StringUtils.trimToNull(ocgId), StringUtils.trimToNull(p[Ocgm.STANDING_POSITION]))
                val dataSet = data.dataSet!![key]

                if (dataSet != null) {
                  dataSet.data.add(ocgmLine)
                } else {
                  data.dataSet!![key] = OcgmList(key, ocgmLine, null)
                }

                data.linesProcessed.incrementAndGet()
              }
            }
          } else {
            log.warn("Missing key in line {} key [{}]", data.index.get(), key)
            data.linesInvalid.incrementAndGet()
          }
        } catch (e: Exception) {
          log.warn("Error in Line {} data [{}]", data.index.get(), p)
          data.linesError.incrementAndGet()
        }
      }
    log.info(
      "Lines total {}, processed {}, dups {}, invalid {}, errors {}",
      data.index.get(),
      data.linesProcessed.get(),
      data.linesDup.get(),
      data.linesInvalid.get(),
      data.linesError.get(),
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(OcgmRepository::class.java)
  }
}
