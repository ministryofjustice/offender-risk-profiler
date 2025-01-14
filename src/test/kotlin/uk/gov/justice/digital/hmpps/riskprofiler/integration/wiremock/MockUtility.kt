package uk.gov.justice.digital.hmpps.riskprofiler.integration.wiremock

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule

class MockUtility {
  companion object {
    private val objectMapper: ObjectMapper = ObjectMapper().registerModules(JavaTimeModule(), kotlinModule())
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    fun getJsonString(obj: Any): String {
      return objectMapper.writer().writeValueAsString(obj)
    }
  }
}