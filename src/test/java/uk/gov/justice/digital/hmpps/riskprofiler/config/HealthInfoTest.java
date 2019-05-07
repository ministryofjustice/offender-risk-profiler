package uk.gov.justice.digital.hmpps.riskprofiler.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.info.BuildProperties;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PathfinderRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrasRepository;
import uk.gov.justice.digital.hmpps.riskprofiler.dao.ViperRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthInfoTest {

    @Mock
    private BuildProperties buildProperties;
    @Mock
    private DataRepositoryFactory dataRepositoryFactory;

    private HealthInfo healthInfo;
    private AtomicBoolean pathfinderAvailable;
    private AtomicBoolean viperAvailable;
    private AtomicBoolean prasAvailable;

    @Before
    public void setup() {
        healthInfo = new HealthInfo();
        ReflectionTestUtils.setField(healthInfo, "buildProperties", buildProperties);
        ReflectionTestUtils.setField(healthInfo, "dataRepositoryFactory", dataRepositoryFactory);

        final PathfinderRepository pathfinderRepository = new PathfinderRepository();
        final ViperRepository viperRepository = new ViperRepository();
        final PrasRepository prasRepository = new PrasRepository();
        pathfinderAvailable = (AtomicBoolean) ReflectionTestUtils.getField(pathfinderRepository, PathfinderRepository.class, "dataAvailable");
        viperAvailable = (AtomicBoolean) ReflectionTestUtils.getField(viperRepository, ViperRepository.class, "dataAvailable");
        prasAvailable = (AtomicBoolean) ReflectionTestUtils.getField(prasRepository, PrasRepository.class, "dataAvailable");

        when(dataRepositoryFactory.getRepositories()).thenReturn(List.of(pathfinderRepository, viperRepository, prasRepository));
        when(buildProperties.getVersion()).thenReturn("1.2.3");
    }

    @Test
    public void testHealthUp() {
        pathfinderAvailable.set(true);
        viperAvailable.set(true);
        prasAvailable.set(true);

        final Health health = healthInfo.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).extracting("version").contains("1.2.3");
    }

    @Test
    public void testHealthOutOfService1() {
        pathfinderAvailable.set(false);
        viperAvailable.set(true);
        prasAvailable.set(true);
        final Health health = healthInfo.health();
        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
    }

    @Test
    public void testHealthOutOfService2() {
        pathfinderAvailable.set(true);
        viperAvailable.set(false);
        prasAvailable.set(true);
        final Health health = healthInfo.health();
        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
    }

    @Test
    public void testHealthOutOfService3() {
        pathfinderAvailable.set(true);
        viperAvailable.set(true);
        prasAvailable.set(false);
        final Health health = healthInfo.health();
        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
    }

    @Test
    public void testHealthOutOfService4() {
        pathfinderAvailable.set(false);
        viperAvailable.set(false);
        prasAvailable.set(true);
        final Health health = healthInfo.health();
        assertThat(health.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
    }
    /*
       public Health health() {
        var allAvailable = dataRepositoryFactory.getRepositories()
                .stream().map (repo -> repo.dataAvailable()).reduce((accumulator, a) -> accumulator && a);
        return Health.status( //allDataAvailable.get()
                allAvailable.orElse(false) ? Status.UP : Status.OUT_OF_SERVICE)
                .withDetail("version", getVersion()).build();
    }

    private String getVersion() {
        return buildProperties == null ? "version not available" : buildProperties.getVersion();
    }
     */
}
