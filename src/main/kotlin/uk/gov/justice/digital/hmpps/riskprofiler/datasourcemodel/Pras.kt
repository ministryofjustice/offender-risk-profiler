package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel

data class Pras(private val nomisId: String) : RiskDataSet {

  override fun getKey() = nomisId

  companion object {
    @JvmField
    var NOMIS_ID_POSITION = 4
  }
}
