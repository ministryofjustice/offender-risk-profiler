package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        OcgmRepository ocgmRepository = new OcgmRepository();
        ocgmRepository.process(ocgmList, "Ocgm-20190204163820000.csv", LocalDateTime.now());
        Optional<Ocgm> isThere = ocgmRepository.getOcgmDataByNomsId("NomisId");
        assertTrue(isThere.isEmpty());

        isThere = ocgmRepository.getOcgmDataByNomsId("A1234AA");
        assertTrue(isThere.isPresent());
        Ocgm ocgm = isThere.get();
        assertEquals(ocgm.getNomisId(), "A1234AA");
        assertEquals(ocgm.getOcgmBand(), "15A");
        assertEquals(ocgm.getStandingWithinOcg(), "PrincipalSubject");

        assertTrue(ocgmRepository.getOcgmDataByNomsId("NotThere").isEmpty());

    }

}
