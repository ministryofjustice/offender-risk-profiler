package uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.google.gson.GsonBuilder

class OAuthMockServer : WireMockServer(9090) {
    private val gson = GsonBuilder().create()

    fun stubGrantToken() {
        stubFor(
            WireMock.post(WireMock.urlEqualTo("/auth/oauth/token"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
                        .withBody(gson.toJson(mapOf("access_token" to "ABCDE", "token_type" to "bearer")))
                )
        )
    }
}

class PrisonMockServer : WireMockServer(8080) {
    private val gson = GsonBuilder().create()

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
}
