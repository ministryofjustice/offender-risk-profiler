package uk.gov.justice.digital.hmpps.riskprofiler.camel

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService
import uk.gov.justice.digital.hmpps.riskprofiler.services.S3FileService

/**
 * Polls the 4 s3 folders for pras, ocgm, ocg and viper
 */
@Service
class CsvProcessorService(private val dataService: DataService, private val fileService: S3FileService) {

  @Scheduled(cron = "\${viper.period}")
  private fun startViperScheduler() {
    val file = fileService.getLatestFile("\${s3.path.viper}}", FileType.VIPER)

    // unmarshal csv

    if (file != null) {
      dataService.process(emptyList(), FileType.VIPER, file)
    }
  }

  @Scheduled(cron = "\${ocg.period}")
  private fun startOcgScheduler() {
    val file = fileService.getLatestFile("\${s3.path.ocg}}", FileType.OCG)

    // unmarshal csv

    if (file != null) {
      dataService.process(emptyList(), FileType.OCG, file)
    }
  }

  @Scheduled(cron = "\${ocgm.period}")
  private fun startOcgmScheduler() {
    val file = fileService.getLatestFile("\${s3.path.ocgm}}", FileType.OCGM)

    // unmarshal csv

    if (file != null) {
      dataService.process(emptyList(), FileType.OCGM, file)
    }
  }

  @Scheduled(cron = "\${pras.period}")
  private fun startPrasScehduler() {
    val file = fileService.getLatestFile("\${s3.path.pras}}", FileType.PRAS)

    // unmarshal csv

    if (file != null) {
      dataService.process(emptyList(), FileType.PRAS, file)
    }
  }
}
