package uk.gov.justice.digital.hmpps.riskprofiler.camel

import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.services.DataService
import uk.gov.justice.digital.hmpps.riskprofiler.services.FileService


/**
 * Polls the 4 s3 folders for pras, ocgm, ocg and viper
 */
@Component
class CsvProcessorRoute(private val dataService: DataService, private val fileService: FileService) : RouteBuilder() {
  override fun configure() {
    val context: CamelContext = DefaultCamelContext()
    context.isStreamCaching = true

    startPrasScehduler()
    startOcgmScheduler()
    startOcgScheduler()
    startViperScheduler()
  }

  private fun startViperScheduler() {
    from("timer://viper-schedule?fixedRate=true&period={{viper.period}}")
      .bean(fileService, "getLatestFile('{{s3.path.viper}}',VIPER)")
      .choice()
      .`when`().simple("\${body} != null")
      .setProperty("fileInfo", simple("\${body}"))
      .setProperty("fileType", simple("VIPER"))
      .setBody(simple("\${body.data}"))
      .unmarshal().csv()
      .bean(dataService, "process")
      .endChoice()
  }

  private fun startOcgScheduler() {
    from("timer://ocg-schedule?fixedRate=true&period={{ocg.period}}&delay={{ocg.delay}}")
      .bean(fileService, "getLatestFile('{{s3.path.ocg}}')")
      .choice()
      .`when`().simple("\${body} != null")
      .setProperty("fileInfo", simple("\${body}"))
      .setProperty("fileType", simple("OCG"))
      .setBody(simple("\${body.data}"))
      .unmarshal().csv()
      .bean(dataService, "process")
      .endChoice()
  }

  private fun startOcgmScheduler() {
    from("timer://ocgm-schedule?fixedRate=true&period={{ocgm.period}}&delay={{ocgm.delay}}")
      .bean(fileService, "getLatestFile('{{s3.path.ocgm}}')")
      .choice()
      .`when`().simple("\${body} != null")
      .setProperty("fileInfo", simple("\${body}"))
      .setProperty("fileType", simple("OCGM"))
      .setBody(simple("\${body.data}"))
      .unmarshal().csv()
      .bean(dataService, "process")
      .endChoice()
  }

  private fun startPrasScehduler() {
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
  }
}
