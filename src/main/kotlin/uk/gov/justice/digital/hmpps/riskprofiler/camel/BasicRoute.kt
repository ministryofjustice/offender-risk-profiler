package uk.gov.justice.digital.hmpps.riskprofiler.camel

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component
class BasicRoute : RouteBuilder() {
  override fun configure() {
    from("direct:a").to("direct:b");
  }
}