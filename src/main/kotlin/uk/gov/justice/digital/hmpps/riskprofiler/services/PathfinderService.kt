package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder

@Service
class PathfinderService @Autowired constructor(@param:Qualifier("pathfinderSystemWebClient") private val webClient: WebClient) {

  fun getBand(nomsId: String): PathFinder? {
    log.debug("Getting noms id {} from pathfinder api", nomsId)
    val uri = String.format("/pathfinder/offender/%s", nomsId)
    return webClient
      .get()
      .uri(uri)
      .retrieve()
      .bodyToMono(PathFinder::class.java)
      .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
      .block()
  }

  companion object {
    private val log = LoggerFactory.getLogger(PathfinderService::class.java)
  }
}
