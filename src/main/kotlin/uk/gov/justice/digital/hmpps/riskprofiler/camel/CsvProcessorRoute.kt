package uk.gov.justice.digital.hmpps.riskprofiler.camel

import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService
import uk.gov.justice.digital.hmpps.riskprofiler.services.FileService

@Component
class CsvProcessorRoute(private val dataService: DataService, private val fileService: FileService) : RouteBuilder() {

  @Value("\${ocgm.delay}")
  private val ocgmDelay: Long = 0

  @Value("\${ocg.delay}")
  private val ocgDelay: Long = 0

  override fun configure() {
    context.isStreamCaching = true
    from("timer://pras-schedule?fixedRate=true&period={{pras.period}}")
      .bean(fileService, "getLatestFile('{{s3.path.pras}}')")
      .choice()
      .`when`().simple("\${body} != null")
      .setProperty("fileInfo", simple("\${body}"))
      .setProperty("fileType", simple("PRAS"))
      .setBody(simple("\${body.data}"))
      .unmarshal().csv()
      .bean(dataService, "process")
      .endChoice()
    from("timer://ocgm-schedule?fixedRate=true&period={{ocgm.period}}")
      .delay(ocgmDelay)
      .bean(fileService, "getLatestFile('{{s3.path.ocgm}}')")
      .choice()
      .`when`().simple("\${body} != null")
      .setProperty("fileInfo", simple("\${body}"))
      .setProperty("fileType", simple("OCGM"))
      .setBody(simple("\${body.data}"))
      .unmarshal().csv()
      .bean(dataService, "process")
      .endChoice()
    from("timer://ocg-schedule?fixedRate=true&period={{ocg.period}}")
      .delay(ocgDelay)
      .bean(fileService, "getLatestFile('{{s3.path.ocg}}')")
      .choice()
      .`when`().simple("\${body} != null")
      .setProperty("fileInfo", simple("\${body}"))
      .setProperty("fileType", simple("OCG"))
      .setBody(simple("\${body.data}"))
      .unmarshal().csv()
      .bean(dataService, "process")
      .endChoice()
    from("timer://viper-schedule?fixedRate=true&period={{viper.period}}")
      .bean(fileService, "getLatestFile('{{s3.path.viper}}')")
      .choice()
      .`when`().simple("\${body} != null")
      .setProperty("fileInfo", simple("\${body}"))
      .setProperty("fileType", simple("VIPER"))
      .setBody(simple("\${body.data}"))
      .unmarshal().csv()
      .bean(dataService, "process")
      .endChoice()
  }
}
