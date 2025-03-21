package uk.gov.justice.digital.hmpps.riskprofiler.file

import com.opencsv.CSVReader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService
import uk.gov.justice.digital.hmpps.riskprofiler.services.PendingFile
import uk.gov.justice.digital.hmpps.riskprofiler.services.S3FileService
import java.io.InputStreamReader

/**
 * Polls the 4 s3 folders for pras, ocgm, ocg and viper
 */
@Service
class CsvProcessorService(
  private val dataService: DataService,
  private val fileService: S3FileService,
  @Value("\${s3.path.ocg}") private val ocgPath: String = "/ocg-data",
  @Value("\${s3.path.ocgm}") private val ocgmPath: String = "/ocgm",
  @Value("\${s3.path.pras}") private val prasPath: String = "/pras",
  @Value("\${s3.path.viper}") private val viperPath: String = "/viper",
) {

  init {
    log.info("Initialising all schedulers")

    if (!initialised) {
      startViperScheduler()
      startOcgScheduler()
      startOcgmScheduler()
      startPrasScehduler()

      initialised = true
    }
  }

  @Scheduled(cron = "\${viper.period}")
  fun startViperScheduler() {
    log.info("Starting VIPER Scheduler - Checking for csv")
    val file = fileService.getLatestViper2File(viperPath, FileType.VIPER)

    if (file != null) {
      dataService.process(unmarshallCsv(file), FileType.VIPER, file)
    }
  }

  @Scheduled(cron = "\${ocg.period}")
  fun startOcgScheduler() {
    log.info("Starting OCG Scheduler - Checking for csv")
    val file = fileService.getLatestFile(ocgPath, FileType.OCG)

    if (file != null) {
      dataService.process(unmarshallCsv(file), FileType.OCG, file)
    }
  }

  @Scheduled(cron = "\${ocgm.period}")
  fun startOcgmScheduler() {
    log.info("Starting OCGM Scheduler - Checking for csv")
    val file = fileService.getLatestFile(ocgmPath, FileType.OCGM)

    if (file != null) {
      dataService.process(unmarshallCsv(file), FileType.OCGM, file)
    }
  }

  @Scheduled(cron = "\${pras.period}")
  fun startPrasScehduler() {
    log.info("Starting PRAS Scheduler - Checking for csv")
    val file = fileService.getLatestFile(prasPath, FileType.PRAS)

    if (file != null) {
      dataService.process(unmarshallCsv(file), FileType.PRAS, file)
    }
  }

  private fun unmarshallCsv(file: PendingFile?): ArrayList<List<String>> {
    val records = ArrayList<List<String>>()
    val csvReader = CSVReader(InputStreamReader(file!!.data))

    var values: Array<String?>?
    while ((csvReader.readNext().also { values = it }) != null) {
      records.add(values!!.toList() as List<String>)
    }
    return records
  }

  companion object {
    @JvmStatic
    private var initialised: Boolean = false

    private val log = LoggerFactory.getLogger(CsvProcessorService::class.java)
  }
}
