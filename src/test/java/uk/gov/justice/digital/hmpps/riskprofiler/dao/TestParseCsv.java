package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestParseCsv {

    @Test
    public void testOCGM() {
        List<String> row1 = Arrays.asList("NomisId", "Birth!", "PNC", "CRO", "OCG", "NewEx", "PG", "PR", "RD", "RT",
                "SL", "1A", "Principal", "Role", "PS", "Nationality", "Female", "SC", "DSCR", "BLA", "MO", "Rel");

        List<String> row2 = Arrays.asList("A1234AA", "Birth!", "PNC", "CRO", "OCG", "NewEx", "PG", "PR", "RD", "RT",
                "SL", "15A", "PrincipalSubject", "Role", "PS", "Nationality", "Female", "SC", "DSCR", "BLA", "MO",
                "Rel");

        List<List<String>> ocgmList = Arrays.asList(row1, row2);
        DataRepository c = new DataRepository();
        c.populateData(ocgmList, "Ocgm-20190204163820000.csv", LocalDateTime.now());
        Optional<Ocgm> isThere = c.getOcgmDataByNomsId("NomisId");
        assertTrue(isThere.isEmpty());

        isThere = c.getOcgmDataByNomsId("A1234AA");
        assertTrue(isThere.isPresent());
        Ocgm ocgm = isThere.get();
        assertEquals(ocgm.getNomisId(), "A1234AA");
        assertEquals(ocgm.getOcgmBand(), "15A");
        assertEquals(ocgm.getStandingWithinOcg(), "PrincipalSubject");

        assertTrue(c.getOcgmDataByNomsId("NotThere").isEmpty());

    }

    @Test
    public void testPRAS() {
        List<String> row1 = new ArrayList<>();
        List<String> row2 = new ArrayList<>();

        for (int i = 0; i < 33; i++) {
            row1.add("Some Value");
            row2.add("Some Value");
        }

        row1.set(11, "NomisId");
        row2.set(11, "A1234AA");
        List<List<String>> prasList = Arrays.asList(row1, row2);

        DataRepository c = new DataRepository();
        c.populateData(prasList, "Pras-20190204163820000.csv", LocalDateTime.now());
        Optional<Pras> isThere = c.getPrasDataByNomsId("NomisId");
        assertTrue(isThere.isEmpty());

        isThere = c.getPrasDataByNomsId("A1234AA");
        assertTrue(isThere.isPresent());
        assertEquals(isThere.get().getNomisId(), "A1234AA");

        assertTrue(c.getPrasDataByNomsId("Nomis3").isEmpty());

    }

    @Test
    public void testPathFinder() {
        List<String> row1 = Arrays.asList("Surname", "Name", "071080", "NomisId", "Status", "status", "Band 1", "est");
        List<String> row2 = Arrays.asList("Surname", "Name", "071080", "A1234AA", "Status", "status", "Band 2", "est");
        List<List<String>> pathFinderList = Arrays.asList(row1, row2);
        DataRepository c = new DataRepository();
        c.populateData(pathFinderList, "Pathfinder-20190204163820000.csv", LocalDateTime.now());


        Optional<PathFinder> isThere = c.getPathfinderDataByNomsId("NomisId");
        assertTrue(isThere.isEmpty());

        isThere = c.getPathfinderDataByNomsId("A1234AA");
        assertTrue(isThere.isPresent());
        var obj = isThere.get();
        assertEquals(obj.getNomisId(), "A1234AA");
        assertEquals(obj.getPathFinderBanding(), "Band 2");

        assertTrue(c.getPathfinderDataByNomsId("No there").isEmpty());
    }

}
