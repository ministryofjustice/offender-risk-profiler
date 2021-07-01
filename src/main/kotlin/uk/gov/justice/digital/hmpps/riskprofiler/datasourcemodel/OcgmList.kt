package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel

data class OcgmList(private val nomisId: String) : RiskDataSet {
  val data: MutableList<Ocgm> = ArrayList()

  override fun getKey(): String {
    return nomisId
  }

  constructor(nomisId: String, ocgm: Ocgm?, ocgms: List<Ocgm>?) : this(nomisId) {
    if (ocgm != null) {
      data.add(ocgm)
    } else if (ocgms != null) {
      data.addAll(ocgms)
    }
  }
}
