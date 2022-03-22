package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel

import java.math.BigDecimal

data class Viper(private val nomisId: String) : RiskDataSet {
  override fun getKey() = nomisId
  var score: BigDecimal? = null

  companion object {
    @JvmField
    var RECORD_ID = 0
    @JvmField
    var NOMIS_ID_POSITION = 1
    @JvmField
    var SCORE_POSITION = 7
  }
}
