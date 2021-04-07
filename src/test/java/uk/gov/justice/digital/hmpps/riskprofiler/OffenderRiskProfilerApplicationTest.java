package uk.gov.justice.digital.hmpps.riskprofiler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = {"test", "localstack-embedded"})
@SpringBootTest
public class OffenderRiskProfilerApplicationTest {

    @Test
    public void testApp() {

    }
}
