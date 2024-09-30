package uk.gov.justice.digital.hmpps.riskprofiler.camel

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.dao.OcgmRepository
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService
import uk.gov.justice.digital.hmpps.riskprofiler.services.S3FileService

/**
 * Polls the 4 s3 folders for pras, ocgm, ocg and viper
 */
@Component
class CsvProcessorService(private val dataService: DataService, private val fileService: S3FileService) {

  @Value("\${s3.path.ocg}")
  private val ocgPath: String = "/ocg-data"

  @Value("\${s3.path.ocgm}")
  private val ocgmPath: String = "/ocgm"

  @Value("\${s3.path.pras}")
  private val prasPath: String = "/pras"

  @Value("\${s3.path.viper}")
  private val viperPath: String = "/viper"

  @Scheduled(cron = "\${viper.period}")
  @Async
  public fun startViperScheduler() {
    log.info("Starting VIPER Scheduler - Checking for csv")
    val file = fileService.getLatestFile(viperPath, FileType.VIPER)

    // unmarshal csv

    if (file != null) {
      dataService.process(emptyList(), FileType.VIPER, file)
    }
  }

  @Scheduled(cron = "\${ocg.period}")
  @Async
  public fun startOcgScheduler() {
    log.info("Starting OCG Scheduler - Checking for csv")
    val file = fileService.getLatestFile(ocgPath, FileType.OCG)

    // unmarshal csv

    if (file != null) {
      dataService.process(emptyList(), FileType.OCG, file)
    }
  }

  @Scheduled(cron = "\${ocgm.period}")
  @Async
  public fun startOcgmScheduler() {
    log.info("Starting OCGM Scheduler - Checking for csv")
    val file = fileService.getLatestFile(ocgmPath, FileType.OCGM)

    // unmarshal csv

    if (file != null) {
      dataService.process(emptyList(), FileType.OCGM, file)
    }
  }

  @Scheduled(cron = "\${pras.period}")
  @Async
  public fun startPrasScehduler() {
    log.info("Starting PRAS Scheduler - Checking for csv")
    val file = fileService.getLatestFile(prasPath, FileType.PRAS)

    // unmarshal csv

    if (file != null) {
      dataService.process(emptyList(), FileType.PRAS, file)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(OcgmRepository::class.java)
  }
}
