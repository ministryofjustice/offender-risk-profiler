package uk.gov.justice.digital.hmpps.riskprofiler.camel

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.services.FileService

@Component
class CsvHousekeepingRoute(private val fileService: FileService) : RouteBuilder() {
  override fun configure() {
//    from("timer://data-deletion-schedule?fixedRate=true&period={{data.deletion.period}}")
//      .bean(fileService, "deleteHistoricalFiles('{{s3.path.ocg}}')")
 //     .bean(fileService, "deleteHistoricalFiles('{{s3.path.ocgm}}')")
 //     .bean(fileService, "deleteHistoricalFiles('{{s3.path.pras}}')")
  //    .bean(fileService, "deleteHistoricalFiles('{{s3.path.viper}}')")
  }
}
