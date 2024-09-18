package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.boot.actuate.health.Status
import org.springframework.boot.info.BuildProperties
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrasRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.ViperRepository
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.RiskDataSet
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(MockitoJUnitRunner::class)
class HealthInfoTest {
  @Mock
  private val buildProperties: BuildProperties? = null

  @Mock
  private val dataRepositoryFactory: DataRepositoryFactory? = null

  private lateinit var healthInfo: HealthInfo
  private lateinit var viperAvailable: AtomicBoolean
  private lateinit var prasAvailable: AtomicBoolean

  @Before
  fun setup() {
    healthInfo = HealthInfo()
    ReflectionTestUtils.setField(healthInfo, "buildProperties", buildProperties)
    ReflectionTestUtils.setField(healthInfo, "dataRepositoryFactory", dataRepositoryFactory)
    val viperRepository = ViperRepository()
    val prasRepository = PrasRepository()
    viperAvailable =
      ReflectionTestUtils.getField(viperRepository, ViperRepository::class.java, "dataAvailable") as AtomicBoolean
    prasAvailable =
      ReflectionTestUtils.getField(prasRepository, PrasRepository::class.java, "dataAvailable") as AtomicBoolean
    Mockito.`when`<List<DataRepository<out RiskDataSet>>>(dataRepositoryFactory!!.getRepositories())
      .thenReturn(listOf(viperRepository, prasRepository))
    Mockito.`when`(buildProperties!!.version).thenReturn("1.2.3")
  }

  @Test
  fun testHealthUp() {
    viperAvailable.set(true)
    prasAvailable.set(true)
    val health = healthInfo.health()
    Assertions.assertThat(health.status).isEqualTo(Status.UP)
    Assertions.assertThat(health.details).extracting("version").isEqualTo("1.2.3")
  }

  @Test
  fun testHealthOutOfService2() {
    viperAvailable.set(false)
    prasAvailable.set(true)
    val health = healthInfo.health()
    Assertions.assertThat(health.status).isEqualTo(Status.OUT_OF_SERVICE)
  }

  @Test
  fun testHealthOutOfService3() {
    viperAvailable.set(true)
    prasAvailable.set(false)
    val health = healthInfo.health()
    Assertions.assertThat(health.status).isEqualTo(Status.OUT_OF_SERVICE)
  }

  @Test
  fun testHealthOutOfService4() {
    viperAvailable.set(false)
    prasAvailable.set(true)
    val health = healthInfo.health()
    Assertions.assertThat(health.status).isEqualTo(Status.OUT_OF_SERVICE)
  }
}
