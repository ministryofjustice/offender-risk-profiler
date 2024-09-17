package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.FileType.Companion.byDataSet
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Viper

@Component("dataRepositoryFactory")
class DataRepositoryFactory(
  ocgmRepository: OcgmRepository,
  ocgRepository: OcgRepository,
  prasRepository: PrasRepository,
  viperRepository: ViperRepository,
) {
  private val ocgmRepository: DataRepository<OcgmList>
  private val ocgRepository: DataRepository<Ocg>
  private val prasRepository: DataRepository<Pras>
  private val viperRepository: DataRepository<Viper>
  fun <T : RiskDataSet> getRepository(type: Class<T>): DataRepository<T> {
    // val repository: DataRepository<T> = null
    return when (byDataSet(type)) {
      FileType.PRAS -> prasRepository as DataRepository<T>
      FileType.OCGM -> ocgmRepository as DataRepository<T>
      FileType.OCG -> ocgRepository as DataRepository<T>
      FileType.VIPER -> viperRepository as DataRepository<T>
    }
    // return repository
  }

  fun getRepositories(): List<DataRepository<out RiskDataSet>> {
    return listOf(
      getRepository(Pras::class.java),
      getRepository(Viper::class.java),
      getRepository(Ocg::class.java),
      getRepository(OcgmList::class.java),
    )
  }

  init {
    this.ocgmRepository = ocgmRepository
    this.ocgRepository = ocgRepository
    this.prasRepository = prasRepository
    this.viperRepository = viperRepository
  }
}
