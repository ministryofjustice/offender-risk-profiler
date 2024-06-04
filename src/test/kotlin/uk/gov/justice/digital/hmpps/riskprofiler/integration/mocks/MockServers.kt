package uk.gov.justice.digital.hmpps.riskprofiler.integration.mocks

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ResourceOAuthMockServer : WireMockServer(9090) {

  companion object {
    @JvmStatic
    val oauthMockServer = ResourceOAuthMockServer().apply { start() }
  }

  private val gson = GsonBuilder().create()

  fun stubGrantToken() {
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/auth/oauth/token"))
        .withHeader("Authorization", EqualToPattern("Basic cmlzay1wcm9maWxlcjpjbGllbnRzZWNyZXQ="))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(gson.toJson(mapOf("access_token" to "ABCDE", "token_type" to "bearer")))
        )
    )
  }
}

class PrisonMockServer : WireMockServer(8080) {

  companion object {
    @JvmStatic
    val prisonMockServer = PrisonMockServer().apply { start() }
  }

  class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate) {
      out.value(DateTimeFormatter.ISO_LOCAL_DATE.format(value))
    }

    override fun read(input: JsonReader): LocalDate = LocalDate.parse(input.nextString())
  }

  private val gson = GsonBuilder().registerTypeAdapter(
    LocalDate::class.java, LocalDateTypeAdapter().nullSafe()
  ).create()

  fun stubIncidents() {
    stubFor(
      WireMock.get(WireMock.urlMatching("/api/offenders/A1234AB/incidents.+"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              gson.toJson(
                listOf(
                  mapOf(
                    "parties" to listOf(
                      mapOf("bookingId" to 1241232, "outcomeCode" to "POR"),
                      "incidentTitle" to "Assault on staff member"
                    )
                  )
                )
              )
            )
        )
    )
  }

  // Warning: will be cached by REDIS !
  fun stubAlerts() {
    val recent = LocalDate.of(2021, 6, 14)
    val alert1 = Alert(
      1234,
      12,
      "A1234AB",
      "POR",
      "desc",
      "DUM",
      "desc",
      "comment",
      recent,
      null,
      false,
      true,
      null,
      null,
      null,
      null,
      1
    )
    val alert2 = Alert(
      5678,
      12,
      "A1234AB",
      "POR",
      "desc",
      "DUM",
      "desc",
      "comment",
      recent,
      null,
      false,
      true,
      null,
      null,
      null,
      null,
      1
    )
    stubFor(
      WireMock.get(WireMock.urlMatching("/api/offenders/.+/alerts/v2\\?alertCodes=.+"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(gson.toJson(listOf(alert1, alert2)))
        )
    )
  }

  fun stubBookingDetails(bookingId: Int) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/bookings/v2?bookingId=$bookingId&legalInfo=true"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              gson.toJson(
                mapOf(
                  "content" to listOf(
                    mapOf(
                      "bookingId" to bookingId,
                      "imprisonmentStatus" to "OTHER"
                    )
                  ),
                  "size" to 1,
                  "number" to 0,
                  "totalElements" to 1
                )
              )
            )
        )
    )
  }

  fun stubOffender(offenderNo: String) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/bookings/offenderNo/$offenderNo"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              gson.toJson(
                mapOf(
                  "bookingId" to 12,
                  "offenderNo" to offenderNo
                )
              )
            )
        )
    )
  }

  fun stubSentences(bookingId: Int) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/offender-sentences/booking/$bookingId/sentenceTerms"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              gson.toJson(
                listOf(
                  mapOf(
                    "bookingId" to bookingId,
                    "lifeSentence" to false
                  )
                )
              )
            )
        )
    )
  }

  fun stubMainOffence(bookingId: Int) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/bookings/$bookingId/mainOffence"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              gson.toJson(
                listOf(
                  mapOf(
                    "bookingId" to bookingId,
                    "offenceDescription" to "MURDER"
                  )
                )
              )
            )
        )
    )
  }

  fun stubPing() {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/health/ping"))
        .willReturn(
          WireMock.aResponse()
            .withBody("pong")
        )
    )
  }
}

class PathfinderMockServer : WireMockServer(8083) {

  companion object {
    @JvmStatic
    val pathfinderMockServer = PathfinderMockServer().apply { start() }
  }

  fun stubPing() {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/health/ping"))
        .willReturn(
          WireMock.aResponse()
            .withBody("pong")
        )
    )
  }

  /**
   * data should be unaltered for band 4
   */
  fun stubPathfinderBand4(nomsId: String) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/pathfinder/offender/$nomsId"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
  {
    "id": 123456,
    "nomsId": "$nomsId",
    "band": 4
  }
              """.trimIndent()
            )
        )
    )
  }

  fun stubPathfinder(nomsId: String) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/pathfinder/offender/$nomsId"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              """
  {
    "id": 123456,
    "nomsId": "$nomsId",
    "band": 2
  }
              """.trimIndent()
            )
        )
    )
  }
}
