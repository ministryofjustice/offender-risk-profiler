package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder
import java.util.Optional

@Service
class PathfinderService @Autowired constructor(@param:Qualifier("pathfinderSystemWebClient") private val webClient: WebClient) {
  protected operator fun <T> get(uri: String?, responseType: Class<T>?): T {
    return webClient.get().uri(uri).retrieve().toEntity(responseType).block().body
  }

  fun getBand(nomsId: String?): Optional<PathFinder> {
    log.debug("Getting noms id {} from pathfinder api", nomsId)
    val uri = String.format("/pathfinder/offender/%s", nomsId)
    return try {
      val map = get<Map<*, *>>(uri, Map::class.java)
      Optional.of(PathFinder((map["nomsId"] as String?)!!, map["band"] as Int?))
    } catch (e: WebClientResponseException.NotFound) {
      Optional.empty()
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(PathfinderService::class.java)
  }
}
