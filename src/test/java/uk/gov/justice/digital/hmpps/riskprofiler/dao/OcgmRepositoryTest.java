package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OcgmRepositoryTest {

    @Test
    public void testOCGM() {
        List<String> row1 = Arrays.asList("NomisId", "Birth!", "PNC", "CRO", "OCG", "NewEx", "PG", "PR", "RD", "RT",
                "SL", "1A", "Principal", "Role", "PS", "Nationality", "Female", "SC", "DSCR", "BLA", "MO", "Rel");

        List<String> row2 = Arrays.asList("A1234AA", "Birth!", "PNC", "CRO", "OCG", "NewEx", "PG", "PR", "RD", "RT",
                "SL", "15A", "PrincipalSubject", "Role", "PS", "Nationality", "Female", "SC", "DSCR", "BLA", "MO",
                "Rel");

        List<List<String>> ocgmList = Arrays.asList(row1, row2);
        DataRepository<OcgmList> repository = new OcgmRepository();
        repository.process(ocgmList, "Ocgm-20190204163820000.csv", LocalDateTime.now());
        Optional<OcgmList> isThere = repository.getByKey("NomisId");
        assertTrue(isThere.isEmpty());

        isThere = repository.getByKey("A1234AA");
        assertTrue(isThere.isPresent());
        OcgmList ocgm = isThere.get();
        assertEquals(ocgm.getKey(), "A1234AA");
        assertThat(ocgm.getData().get(0).getStandingWithinOcg()).isEqualTo("RD");

        assertTrue(repository.getByKey("NotThere").isEmpty());

    }

}
