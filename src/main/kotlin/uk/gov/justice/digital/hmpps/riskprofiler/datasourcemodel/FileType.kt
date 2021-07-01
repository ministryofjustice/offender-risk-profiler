package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel

import java.util.Arrays

enum class FileType(val type: Class<out RiskDataSet?>) {
  PRAS(Pras::class.java), OCGM(OcgmList::class.java), OCG(Ocg::class.java), VIPER(Viper::class.java);

  companion object {
    @JvmStatic
    fun byDataSet(clazz: Class<out RiskDataSet?>): FileType {
      return Arrays.stream(values())
        .filter { ft: FileType -> ft.type == clazz }
        .findFirst().orElse(null)
    }
  }
}
