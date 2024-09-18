package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDateTime

class OcgmRepositoryTest {
  @Test
  fun testOCGM() {
    val row1 = listOf(
      "Nomis No.",
      "Establishment",
      "Region",
      "Prison Category",
      "OCG ID Number",
      "Cell Location",
      "Earliest Possible Release Date",
      "Release Date",
      "Release Type",
      "Main Offence",
      "Sentence Length In Days",
      "Surname",
      "Forenames",
      "Date of Birth",
      "PNC Number",
      "PND Id",
      "CRO Number",
      "CHS Number",
      "OCGM Surname",
      "OCGM Forenames",
      "OCGM Date of Birth",
      "OCGM PNC Number",
      "OCGM Nominal ID",
      "Gender",
      "Aliases / Nicknames",
      "Standing Within Ocg",
      "Role: Corrupter",
      "Can the data on this record be disseminated?",
      "Priority Group",
    )
    val row2 = listOf(
      "A5015DY",
      "",
      "",
      "",
      "001/0010058",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "Principal Subject",
      "",
      "",
      "",
    )
    val ocgmList = listOf(row1, row2)
    val repository = OcgmRepository()
    repository.process(ocgmList, "Ocgm-20190204163820000.csv", LocalDateTime.now())
    val ocgm = repository.getByKey("A5015DY").orElseThrow()
    Assertions.assertThat(ocgm).isNotNull
    Assertions.assertThat(ocgm.getKey()).isEqualTo("A5015DY")
    Assertions.assertThat(ocgm.data[0].standingWithinOcg).isEqualTo("Principal Subject")
    Assertions.assertThat(repository.getByKey("NotThere")).isEmpty

    // the other data map should initially be empty
    Assertions.assertThat(repository.standbyData.linesProcessed.get()).isEqualTo(0)
    Assertions.assertThat(repository.standbyData.dataSet).isNull()

    // now load new data
    val newRow2 = listOf(
      "A5015DY",
      "",
      "",
      "",
      "001/0010060",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "New Subject",
      "",
      "",
      "",
    )
    val reloader = Thread { repository.process(listOf(row1, newRow2), "Ocgm-new.csv", LocalDateTime.now()) }
    reloader.start()

    // While data is loading, read repeatedly from repository: data should switch from the old to the new at some point but never be missing
    while (reloader.isAlive) {
      val newValue = repository.getByKey("A5015DY").orElseThrow()
      val standingWithinOcg = newValue.data[0].standingWithinOcg
      Assertions.assertThat("Principal Subject" == standingWithinOcg || "New Subject" == standingWithinOcg).isTrue
    }
    // By now we have switched to the new
    val newValue = repository.getByKey("A5015DY").orElseThrow()
    val standingWithinOcg = newValue.data[0].standingWithinOcg
    Assertions.assertThat(standingWithinOcg).isEqualTo("New Subject")
  }
}
