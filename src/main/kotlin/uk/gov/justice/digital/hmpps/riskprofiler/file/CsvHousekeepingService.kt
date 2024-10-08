package uk.gov.justice.digital.hmpps.riskprofiler.file

import org.apache.commons.lang3.time.DateUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.services.S3FileService

@Service
class CsvHousekeepingService(private val fileService: S3FileService) {

  @Value("\${s3.path.ocg}")
  private val ocgPath: String = "/ocg-data"

  @Value("\${s3.path.ocgm}")
  private val ocgmPath: String = "/ocgm"

  @Value("\${s3.path.pras}")
  private val prasPath: String = "/pras"

  @Value("\${s3.path.viper}")
  private val viperPath: String = "/viper"

  @Scheduled(fixedRate = DateUtils.MILLIS_PER_DAY)
  public fun cleanupHistoricalCsvFiles() {
    fileService.deleteHistoricalFiles(ocgPath)
    fileService.deleteHistoricalFiles(prasPath)
    fileService.deleteHistoricalFiles(ocgmPath)
    fileService.deleteHistoricalFiles(viperPath)
  }
}
