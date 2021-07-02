package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel

data class Ocg(private val ocgId: String) : RiskDataSet {

  constructor(ocgId: String, ocgmBand: String?) : this(ocgId) {
    this.ocgmBand = ocgmBand
  }

  var ocgmBand: String? = null

  override fun getKey(): String {
    return ocgId
  }

  companion object {
    @JvmField
    var OCG_ID_POSITION = 0

    @JvmField
    var OCGM_BAND_POSITION = 1
  }
}
