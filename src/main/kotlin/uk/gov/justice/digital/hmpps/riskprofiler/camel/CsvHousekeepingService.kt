package uk.gov.justice.digital.hmpps.riskprofiler.camel

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.services.FileService

@Service
class CsvHousekeepingService(private val fileService: FileService) {

  @Scheduled(fixedRateString = "\${data.deletion.period}")
  fun cleanupHistoricalCsvFiles() {
    fileService.deleteHistoricalFiles("\${s3.path.ocg}")
    fileService.deleteHistoricalFiles("\${s3.path.ocgm}")
    fileService.deleteHistoricalFiles("\${s3.path.pras}")
    fileService.deleteHistoricalFiles("\${s3.path.viper}")
  }
}
