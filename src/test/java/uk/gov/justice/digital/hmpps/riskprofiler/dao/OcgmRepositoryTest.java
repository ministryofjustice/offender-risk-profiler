package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class OcgmRepositoryTest {

    @Test
    public void testOCGM() {
        final var row1 = Arrays.asList("Nomis No.","Establishment","Region","Prison Category","OCG ID Number","Cell Location","Earliest Possible Release Date","Release Date","Release Type","Main Offence","Sentence Length In Days","Surname","Forenames","Date of Birth","PNC Number","PND Id","CRO Number","CHS Number","OCGM Surname","OCGM Forenames","OCGM Date of Birth","OCGM PNC Number","OCGM Nominal ID","Gender","Aliases / Nicknames","Standing Within Ocg","Role: Corrupter","Can the data on this record be disseminated?","Priority Group");
        final var row2 = Arrays.asList("A5015DY","","","","001/0010058","","","","","","","","","","","","","","","","","","","","","Principal Subject","","","");

        final var ocgmList = Arrays.asList(row1, row2);
        final var repository = new OcgmRepository();
        repository.process(ocgmList, "Ocgm-20190204163820000.csv", LocalDateTime.now());
        var ocgm = repository.getByKey("A5015DY").orElseThrow();
        assertThat(ocgm).isNotNull();
        assertThat(ocgm.getKey()).isEqualTo( "A5015DY");
        assertThat(ocgm.getData().get(0).getStandingWithinOcg()).isEqualTo("Principal Subject");
        assertTrue(repository.getByKey("NotThere").isEmpty());
    }

}
