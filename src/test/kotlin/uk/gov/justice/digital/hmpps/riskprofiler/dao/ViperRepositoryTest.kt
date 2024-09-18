package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class ViperRepositoryTest {
  private val repository = ViperRepository()

  @Before
  fun setup() {
    val row1 = listOf(
      "1419276",
      "A5015DY",
      "09/06/1980",
      "2.474015192",
      "0.337144724",
      "-0.853068861",
      "0.185042824",
      "1.620946332",
      "0.384587326",
      "3",
      "TRUE",
    )
    val row2 = listOf(
      "1431076",
      "A5015DX",
      "04/04/1990",
      "2.202153377",
      "0.25044519",
      "-0.598150198",
      "0.18499339",
      "1.604003179",
      "0.311360479",
      "3",
      "TRUE",
    )
    val row3 = listOf(
      "1433408",
      "A5015DZ",
      "01/05/1980",
      "2.661700472",
      "0.420323148",
      "-1.062636224",
      "0.185173187",
      "1.599064248",
      "0.459304537",
      "3",
      "TRUE",
    )
    val viperList = listOf(row1, row2, row3)
    repository.process(viperList, "Viper.csv", LocalDateTime.now())
  }

  @Test
  fun testViperLine1() {
    val viper1 = repository.getByKey("A5015DY").orElseThrow()
    Assertions.assertThat(viper1).isNotNull
    Assertions.assertThat(viper1.getKey()).isEqualTo("A5015DY")
    Assertions.assertThat(viper1.score).isEqualTo(BigDecimal("5.057874480976224"))
  }

  @Test
  fun testViperOtherLines() {
    Assertions.assertThat(repository.getByKey("A5015DX").orElseThrow().score).isEqualTo(BigDecimal("4.97290004006141"))
    Assertions.assertThat(repository.getByKey("A5015DZ").orElseThrow().score).isEqualTo(BigDecimal("4.948399782238044"))
  }
}
