package uk.gov.justice.digital.hmpps.riskprofiler.camel

import org.apache.commons.lang3.time.DateUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.services.S3FileService

@Component
class CsvHousekeepingService(private val fileService: S3FileService) {

  @Scheduled(fixedRate = DateUtils.MILLIS_PER_DAY)
  fun cleanupHistoricalCsvFiles() {
    fileService.deleteHistoricalFiles("\${s3.path.ocg}")
    fileService.deleteHistoricalFiles("\${s3.path.ocgm}")
    fileService.deleteHistoricalFiles("\${s3.path.pras}")
    fileService.deleteHistoricalFiles("\${s3.path.viper}")
  }
}
