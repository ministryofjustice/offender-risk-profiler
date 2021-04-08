package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(profiles = {"test", "localstack"})
@Transactional
public class PrisonSupportedRepositoryTest {

    @Autowired
    private PrisonSupportedRepository repository;

    @Test
    public void givenATransientWhenPersistedItShoudBeRetrievableById() {

        final var transientEntity = transientEntity();

        final var persistedEntity = repository.save(transientEntity);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();

        final var retrievedEntity = repository.findById(transientEntity.getPrisonId()).orElseThrow();

        assertThat(retrievedEntity).isEqualTo(transientEntity);
    }

    private PrisonSupported transientEntity() {
        return PrisonSupported
                .builder()
                .prisonId("LEI")
                .startDateTime(LocalDateTime.of(2020, 4, 30, 14, 40))
                .build();
    }
}
