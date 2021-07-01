package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.junit.Assert
import org.junit.Test
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras
import java.time.LocalDateTime

class PrasRepositoryTest {
  @Test
  fun testPRAS() {
    val row1: MutableList<String> = ArrayList()
    val row2: MutableList<String> = ArrayList()
    for (i in 0..32) {
      row1.add("Some Value")
      row2.add("Some Value")
    }
    row1[Pras.NOMIS_ID_POSITION] = "NomisId"
    row2[Pras.NOMIS_ID_POSITION] = "A1234AA"
    val prasList = listOf(row1, row2)
    val repository: DataRepository<Pras> = PrasRepository()
    repository.process(prasList, "Pras-20190204163820000.csv", LocalDateTime.now())
    var isThere = repository.getByKey("NomisId")
    Assert.assertTrue(isThere.isEmpty)
    isThere = repository.getByKey("A1234AA")
    Assert.assertTrue(isThere.isPresent)
    Assert.assertEquals(isThere.get().getKey(), "A1234AA")
    Assert.assertTrue(repository.getByKey("Nomis3").isEmpty)
  }
}
