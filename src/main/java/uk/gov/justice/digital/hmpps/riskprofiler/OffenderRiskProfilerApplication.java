package uk.gov.justice.digital.hmpps.riskprofiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
public class OffenderRiskProfilerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OffenderRiskProfilerApplication.class, args);
    }

}
