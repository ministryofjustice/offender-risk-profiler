package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestParseCsv {

    @Test
    public void testOCGM() {
        List<String> row1 = Arrays.asList("NomisId1", "Birth!", "PNC", "CRO", "OCG", "NewEx", "PG", "PR", "RD", "RT",
                "SL", "1A", "Principal", "Role", "PS", "Nationality", "Female", "SC", "DSCR", "BLA", "MO", "Rel");

        List<String> row2 = Arrays.asList("NomisId2", "Birth!", "PNC", "CRO", "OCG", "NewEx", "PG", "PR", "RD", "RT",
                "SL", "15A", "PrincipalSubject", "Role", "PS", "Nationality", "Female", "SC", "DSCR", "BLA", "MO",
                "Rel");

        List<List<String>> ocgmList = Arrays.asList(row1, row2);
        DataRepository c = new DataRepository();
        c.populateMap(ocgmList, "Ocgm");
        Optional<Ocgm> isThere = c.getOcgmDataByNomsId("NomisId1");
        assertTrue(isThere.isPresent());
        Ocgm ocgm = isThere.get();
        assertEquals(ocgm.getNomisId(), "NomisId1");
        assertEquals(ocgm.getOcgmBand(), "1A");
        assertEquals(ocgm.getStandingWithinOcg(), "Principal");
        isThere = c.getOcgmDataByNomsId("NomisId2");
        assertTrue(isThere.isPresent());
        ocgm = isThere.get();
        assertEquals(ocgm.getNomisId(), "NomisId2");
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

        row1.set(11, "Nomis1");
        row2.set(11, "Nomis2");
        List<List<String>> prasList = Arrays.asList(row1, row2);

        DataRepository c = new DataRepository();
        c.populateMap(prasList, "PRAS");
        Optional<Pras> isThere = c.getPrasDataByNomsId("Nomis1");
        assertTrue(isThere.isPresent());
        assertEquals(isThere.get().getNomisId(), "Nomis1");

        isThere = c.getPrasDataByNomsId("Nomis2");
        assertTrue(isThere.isPresent());
        assertEquals(isThere.get().getNomisId(), "Nomis2");

        assertTrue(c.getOcgmDataByNomsId("Nomis3").isEmpty());

    }

    @Test
    public void testPathFinder() {
        List<String> row1 = Arrays.asList("Surname", "Name", "071080", "NomisId1", "Status", "status", "Band 1", "est");
        List<String> row2 = Arrays.asList("Surname", "Name", "071080", "NomisId2", "Status", "status", "Band 2", "est");
        List<List<String>> pathFinderList = Arrays.asList(row1, row2);
        DataRepository c = new DataRepository();
        c.populateMap(pathFinderList, "PATHFINDER");

        Optional<PathFinder> isThere = c.getPathfinderDataByNomsId("NomisId1");
        assertTrue(isThere.isPresent());
        PathFinder obj = isThere.get();
        assertEquals(obj.getNomisId(), "NomisId1");
        assertEquals(obj.getPathFinderBanding(), "Band 1");

        isThere = c.getPathfinderDataByNomsId("NomisId2");
        assertTrue(isThere.isPresent());
        obj = isThere.get();
        assertEquals(obj.getNomisId(), "NomisId2");
        assertEquals(obj.getPathFinderBanding(), "Band 2");

        assertTrue(c.getPathfinderDataByNomsId("No there").isEmpty());
    }

}
