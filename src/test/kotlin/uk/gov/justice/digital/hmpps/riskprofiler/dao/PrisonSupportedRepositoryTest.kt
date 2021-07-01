package uk.gov.justice.digital.hmpps.riskprofiler.dao

import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(profiles = ["test", "localstack"])
@Transactional
class PrisonSupportedRepositoryTest {
  @Autowired
  private lateinit var repository: PrisonSupportedRepository
  @Test
  fun givenATransientWhenPersistedItShouldBeRetrievableById() {
    val transientEntity = transientEntity()
    repository.save(transientEntity)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    val retrievedEntity = repository.findById(transientEntity.prisonId).orElseThrow()!!
    Assertions.assertThat(retrievedEntity).isEqualTo(transientEntity)
  }

  private fun transientEntity(): PrisonSupported {
    return PrisonSupported("LEI", LocalDateTime.of(2020, 4, 30, 14, 40))
  }
}
