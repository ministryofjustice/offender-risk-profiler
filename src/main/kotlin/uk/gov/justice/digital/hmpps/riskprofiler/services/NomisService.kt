package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert
import uk.gov.justice.digital.hmpps.riskprofiler.model.BookingDetails
import uk.gov.justice.digital.hmpps.riskprofiler.model.IncidentCase
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderBooking
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderSentenceTerms
import uk.gov.justice.digital.hmpps.riskprofiler.model.PagingAndSortingDto
import java.util.Objects
import java.util.stream.Collectors
import javax.validation.constraints.NotNull

@Service
class NomisService(
  private val webClientCallHelper: WebClientCallHelper,
  @param:Value("\${app.assaults.incident.types:ASSAULT}") private val incidentTypes: List<String?>,
  @param:Value("\${app.assaults.participation.roles}") private val participationRoles: List<String>
) {
  @Cacheable("escapeAlert")
  fun getEscapeListAlertsForOffender(nomsId: String?): List<Alert> {
    log.info("Getting escape list alerts for noms id {}", nomsId)
    return getAlertsForOffender(nomsId, ESCAPE_LIST_ALERT_TYPES)
  }

  @CacheEvict("escapeAlert")
  fun evictEscapeListAlertsCache(nomsId: String?) {
    log.info("Evicting {} from escapeAlert cache", nomsId)
  }

  @Cacheable("socAlert")
  fun getSocListAlertsForOffender(nomsId: String?): List<Alert> {
    log.info("Getting soc list alerts for noms id {}", nomsId)
    return getAlertsForOffender(nomsId, SOC_ALERT_TYPES)
  }

  @CacheEvict("socAlert")
  fun evictSocListAlertsCache(nomsId: String?) {
    log.info("Evicting {} from socAlert cache", nomsId)
  }

  fun getAlertsForOffender(nomsId: String?, alertTypes: List<String?>): List<Alert> {
    log.info("Getting alerts for noms id {} and types {}", nomsId, alertTypes)
    val types = alertTypes.stream()
      .map { alertType: String? -> String.format("alertCode:eq:'%s'", alertType) }
      .collect(Collectors.joining(",or:"))
    val uriAlertsForOffenderByType =
      String.format("/api/offenders/%1\$s/alerts?query=%2\$s&latestOnly=false", nomsId, types)
    return webClientCallHelper.getForList(uriAlertsForOffenderByType, ALERTS).body!!
  }

  fun getSentencesForOffender(bookingId: Long?): List<OffenderSentenceTerms> {
    log.info("Getting sentences for bookingId {}", bookingId)
    val uri = String.format("/api/offender-sentences/booking/%s/sentenceTerms", bookingId)
    return webClientCallHelper.getForList(uri, SENTENCE_TERMS).body!!
  }

  @Cacheable("incident")
  fun getIncidents(nomsId: @NotNull String?): List<IncidentCase> {
    log.info(
      "Getting incidents for noms id {} and type {}, with roles of {}",
      nomsId,
      incidentTypes,
      participationRoles
    )
    val incidentTypesStr = incidentTypes.stream()
      .map { incidentType: String? -> String.format("incidentType=%s", incidentType) }
      .collect(Collectors.joining("&"))
    val participationRolesStr = participationRoles.stream()
      .map { participationRole: String? -> String.format("participationRoles=%s", participationRole) }
      .collect(Collectors.joining("&"))
    val uriIncidentsForOffender =
      String.format("/api/offenders/%s/incidents?%s&%s", nomsId, incidentTypesStr, participationRolesStr)
    return webClientCallHelper.getForList(uriIncidentsForOffender, INCIDENTS).body!!
  }

  @CacheEvict("incident")
  fun evictIncidentsCache(nomsId: String?) {
    log.info("Evicting {} from incident cache", nomsId)
  }

  fun getOffendersAtPrison(prisonId: @NotNull String?): List<String> {
    val uri = String.format("/api/bookings?query=agencyId:eq:'%s'", prisonId)
    val results: List<Map<*, *>>
    results = try {
      webClientCallHelper.getWithPaging(uri, PagingAndSortingDto(0L, Int.MAX_VALUE.toLong()), MAP).body!!
    } catch (e: WebClientResponseException.NotFound) {
      log.warn("Prison does not exist")
      return listOf()
    }
    return results.stream().map { m: Map<*, *> -> m["offenderNo"] as String }
      .collect(Collectors.toList())
  }

  fun getBookingDetails(bookingId: Long?): List<OffenderBooking> {
    log.info("Getting details for bookingId {}", bookingId)
    val uri = String.format("/api/bookings?bookingId=%s", bookingId)
    return webClientCallHelper.getForList(uri, BOOKING_DETAILS).body!!
  }

  fun getMainOffences(bookingId: Long?): List<String> {
    val uri = String.format("/api/bookings/%d/mainOffence", bookingId)
    val results = webClientCallHelper.getForList(uri, MAP).body
    return results.stream().map { m: Map<*, *> -> m["offenceDescription"] as String }
      .collect(Collectors.toList())
  }

  fun getOffender(bookingId: @NotNull Long?): String? {
    val uri = String.format("/api/bookings/%d?basicInfo=true", bookingId)
    val result = webClientCallHelper.get(uri, BookingDetails::class.java)
    return result.offenderNo
  }

  fun getBooking(nomsId: @NotNull String?): Long {
    val uri = String.format("/api/bookings/offenderNo/%s", nomsId)
    val (bookingId) = webClientCallHelper.get(uri, BookingDetails::class.java)
    return bookingId!!
  }

  fun getPartiesOfIncident(incidentId: @NotNull Long?): List<String?> {
    val uri = String.format("/api/incidents/%d", incidentId)
    val incident: IncidentCase
    incident = try {
      webClientCallHelper.get(uri, IncidentCase::class.java)
    } catch (nf: WebClientResponseException.NotFound) {
      // 404: incident not found, OR has no questions answered yet
      return emptyList<String>()
    }
    return if (incident.parties == null || !incidentTypes.contains(incident.incidentType)) {
      emptyList<String>()
    } else incident.parties!!.stream()
      .map { (bookingId) -> bookingId?.let { getOffender(it) } }
      .filter { obj: String? -> Objects.nonNull(obj) }
      .collect(Collectors.toList())
  }

  companion object {
    private val log = LoggerFactory.getLogger(NomisService::class.java)
    private val ALERTS: ParameterizedTypeReference<List<Alert>> = object : ParameterizedTypeReference<List<Alert>>() {}
    private val INCIDENTS: ParameterizedTypeReference<List<IncidentCase>> =
      object : ParameterizedTypeReference<List<IncidentCase>>() {}
    private val MAP: ParameterizedTypeReference<List<Map<*, *>>> =
      object : ParameterizedTypeReference<List<Map<*, *>>>() {}
    private val BOOKING_DETAILS: ParameterizedTypeReference<List<OffenderBooking>> =
      object : ParameterizedTypeReference<List<OffenderBooking>>() {}
    private val SENTENCE_TERMS: ParameterizedTypeReference<List<OffenderSentenceTerms>> =
      object : ParameterizedTypeReference<List<OffenderSentenceTerms>>() {}
    val ESCAPE_LIST_ALERT_TYPES = java.util.List.of("XER", "XEL")
    val SOC_ALERT_TYPES = java.util.List.of(
      "PL3", "PVN", "HPI", "XCO", "XD", "XEAN", "XEBM",
      "XFO", "XGANG", "XOCGN", "XP", "XSC"
    )
  }
}
