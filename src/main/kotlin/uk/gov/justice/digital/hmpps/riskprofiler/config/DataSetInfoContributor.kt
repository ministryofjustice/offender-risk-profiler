package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet
import java.time.format.DateTimeFormatter

@Component
class DataSetInfoContributor @Autowired constructor(private val dataRepositoryFactory: DataRepositoryFactory) :
  InfoContributor {
  override fun contribute(builder: Info.Builder) {
    val results = HashMap<String, String>()
    dataRepositoryFactory.getRepositories().forEach { dataRepository: DataRepository<out RiskDataSet?> ->
      val data = dataRepository.data
      if (data.fileType != null) {
        results[data.fileType.toString()] = String.format(
          "Processed (%s): %d, Dups: %d, Invalid: %d, Error: %d, Total: %d",
          data.fileTimestamp!!.format(DateTimeFormatter.ISO_DATE_TIME),
          data.linesProcessed.get(), data.linesDup.get(), data.linesInvalid.get(), data.linesError.get(),
          data.index.get()
        )
      }
    }
    builder.withDetail("riskData", results)
  }
}
