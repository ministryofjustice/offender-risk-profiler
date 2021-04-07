package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PreviousProfile;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(profiles = {"test", "localstack-embedded"})
@Transactional
public class PreviousProfileRepositoryTest {

    @Autowired
    private PreviousProfileRepository repository;

    @Test
    public void givenATransientWhenPersistedItShoudBeRetrievableById() {

        final var transientEntity = transientEntity();

        final var persistedEntity = repository.save(transientEntity);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();

        final var retrievedEntity = repository.findById(transientEntity.getOffenderNo()).orElseThrow();

        // equals only compares the business key columns: staffId
        assertThat(retrievedEntity).isEqualTo(transientEntity);
    }

    @Test
    public void givenAPersistentInstanceThenNullableValuesAreUpdateable() {

        final var entity = repository.save(transientEntity());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        repository.findById(entity.getOffenderNo());

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();

        repository.findById(entity.getOffenderNo());
    }

    private PreviousProfile transientEntity() {
        return PreviousProfile
                .builder()
                .offenderNo("A1234AA")
                .executeDateTime(LocalDateTime.of(2020, 3, 30, 14, 40))
                .build();
    }
}
