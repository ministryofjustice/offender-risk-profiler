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
import uk.gov.justice.digital.hmpps.riskprofiler.model.PreviousProfile
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(profiles = ["test", "localstack"])
@Transactional
class PreviousProfileRepositoryTest {
  @Autowired
  private lateinit var repository: PreviousProfileRepository

  @Test
  fun givenATransientWhenPersistedItShoudBeRetrievableById() {
    val transientEntity = transientEntity()
    repository.save(transientEntity)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    val retrievedEntity = repository.findById(transientEntity.offenderNo).orElseThrow()!!

    // equals only compares the business key columns: staffId
    Assertions.assertThat(retrievedEntity).isEqualTo(transientEntity)
  }

  @Test
  fun givenAPersistentInstanceThenNullableValuesAreUpdateable() {
    val (offenderNo) = repository.save(transientEntity())
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    repository.findById(offenderNo)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
    repository.findById(offenderNo)
  }

  private fun transientEntity(): PreviousProfile {
    return PreviousProfile(
      "A1234AA", "", "", "",
      LocalDateTime.of(2020, 3, 30, 14, 40)
    )
  }
}
