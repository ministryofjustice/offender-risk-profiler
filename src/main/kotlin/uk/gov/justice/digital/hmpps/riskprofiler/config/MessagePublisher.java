package uk.gov.justice.digital.hmpps.riskprofiler.config;

public interface MessagePublisher {

    void publish(final String message);
}
