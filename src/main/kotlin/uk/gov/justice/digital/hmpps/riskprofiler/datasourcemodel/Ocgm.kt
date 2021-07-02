package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel

data class Ocgm(private val nomisId: String) {
  constructor (nomisId: String, ocgId: String?, standingWithinOcg: String?) : this(nomisId) {
    this.ocgId = ocgId
    this.standingWithinOcg = standingWithinOcg
  }

  var ocgId: String? = null
  var standingWithinOcg: String? = null

  companion object {
    @JvmField
    var NOMIS_ID_POSITION = 0

    @JvmField
    var OCG_ID_POSITION = 4

    @JvmField
    var STANDING_POSITION = 25
  }
}
