package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ViperRepositoryTest {
    private final ViperRepository repository = new ViperRepository();

    @Before
    public void setup() {
        final var row1 = Arrays.asList("1419276", "A5015DY", "09/06/1980", "2.474015192", "0.337144724","-0.853068861","0.185042824","1.620946332","0.384587326","3","TRUE");
        final var row2 = Arrays.asList("1431076", "A5015DX", "04/04/1990", "2.202153377", "0.25044519","-0.598150198","0.18499339","1.604003179","0.311360479","3","TRUE");
        final var row3 = Arrays.asList("1433408", "A5015DZ", "01/05/1980", "2.661700472", "0.420323148","-1.062636224","0.185173187","1.599064248","0.459304537","3","TRUE");

        final var viperList = Arrays.asList(row1, row2, row3);
        repository.process(viperList, "Viper.csv", LocalDateTime.now());
    }

    @Test
    public void testViperLine1() {
        var viper1 = repository.getByKey("A5015DY").orElseThrow();
        assertThat(viper1).isNotNull();
        assertThat(viper1.getKey()).isEqualTo("A5015DY");
        assertThat(viper1.getScore()).isEqualTo(new BigDecimal("5.057874480976224"));
    }

    @Test
    public void testViperOtherLines() {
        assertThat(repository.getByKey("A5015DX").orElseThrow().getScore()).isEqualTo(new BigDecimal("4.97290004006141"));
        assertThat(repository.getByKey("A5015DZ").orElseThrow().getScore()).isEqualTo(new BigDecimal("4.948399782238044"));
    }

}
