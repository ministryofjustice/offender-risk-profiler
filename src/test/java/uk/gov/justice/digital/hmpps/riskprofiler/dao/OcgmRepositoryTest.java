package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
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
        assertThat(repository.getByKey("NotThere")).isEmpty();

        // the other data map should initially be empty
        assertThat(repository.getStandbyData().getLinesProcessed().get()).isEqualTo(0);
        assertThat(repository.getStandbyData().getDataSet()).isNull();

        // now load new data
        final var newRow2 = Arrays.asList("A5015DY", "", "", "", "001/0010060", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "New Subject", "", "", "");
        final Thread reloader = new Thread(() -> repository.process(Arrays.asList(row1, newRow2), "Ocgm-new.csv", LocalDateTime.now()) );
        reloader.start();

       // While data is loading, read repeatedly from repository: data should switch from the old to the new at some point but never be missing
        while (reloader.isAlive()) {
            final OcgmList newValue = repository.getByKey("A5015DY").orElseThrow();
            final String standingWithinOcg = newValue.getData().get(0).getStandingWithinOcg();
            assertThat("Principal Subject".equals(standingWithinOcg) || "New Subject".equals(standingWithinOcg)).isTrue();
        }
        // By now we have switched to the new
        final OcgmList newValue = repository.getByKey("A5015DY").orElseThrow();
        final String standingWithinOcg = newValue.getData().get(0).getStandingWithinOcg();
        assertThat(standingWithinOcg).isEqualTo("New Subject");
    }
}
