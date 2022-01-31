package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.toEntity
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder
import java.util.Optional

@Service
class PathfinderService @Autowired constructor(@param:Qualifier("pathfinderSystemWebClient") private val webClient: WebClient) {
  protected operator fun get(uri: String): Optional<PathFinder> {
    return webClient.get().uri(uri).retrieve().toEntity<PathFinder>().blockOptional().map { it.body }
  }

  fun getBand(nomsId: String): Optional<PathFinder> {
    log.debug("Getting noms id {} from pathfinder api", nomsId)
    val uri = String.format("/pathfinder/offender/%s", nomsId)
    return try {
      get(uri)
    } catch (e: WebClientResponseException.NotFound) {
      Optional.empty()
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(PathfinderService::class.java)
  }
}
