package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrasRepositoryTest {

    @Test
    public void testPRAS() {
        List<String> row1 = new ArrayList<>();
        List<String> row2 = new ArrayList<>();

        for (int i = 0; i < 33; i++) {
            row1.add("Some Value");
            row2.add("Some Value");
        }

        row1.set(Pras.NOMIS_ID_POSITION, "NomisId");
        row2.set(Pras.NOMIS_ID_POSITION, "A1234AA");
        final var prasList = Arrays.asList(row1, row2);

        DataRepository<Pras> repository = new PrasRepository();
        repository.process(prasList, "Pras-20190204163820000.csv", LocalDateTime.now());
        Optional<Pras> isThere = repository.getByKey("NomisId");
        assertTrue(isThere.isEmpty());

        isThere = repository.getByKey("A1234AA");
        assertTrue(isThere.isPresent());
        assertEquals(isThere.get().getKey(), "A1234AA");

        assertTrue(repository.getByKey("Nomis3").isEmpty());

    }

}
