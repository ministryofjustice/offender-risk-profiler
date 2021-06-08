package uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock

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

class OAuthMockServer : WireMockServer(9090) {

  companion object {
    @JvmStatic
    val oauthMockServer = OAuthMockServer()
    val dum = oauthMockServer.start()
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
    val prisonMockServer = PrisonMockServer()
    val dum = prisonMockServer.start()
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
      WireMock.get(WireMock.urlMatching("/api/incidents/.+"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(
              gson.toJson(
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
  }

  // Warning: will be cached by REDIS !
  fun stubAlerts() {
    val recent = LocalDate.now().minusDays(10)
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
      WireMock.get(WireMock.urlMatching("/api/offenders/.+/alerts\\?query=alertCode:eq:.+&latestOnly=false"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(gson.toJson(listOf(alert1, alert2)))
        )
    )
  }
}
