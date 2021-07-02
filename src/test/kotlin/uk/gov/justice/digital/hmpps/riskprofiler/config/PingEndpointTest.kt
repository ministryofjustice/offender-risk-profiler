package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.assertj.core.api.Assertions
import org.junit.Test

class PingEndpointTest {
  @Test
  fun ping() {
    Assertions.assertThat(PingEndpoint().ping()).isEqualTo("pong")
  }
}
