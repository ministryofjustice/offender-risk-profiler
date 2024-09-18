package uk.gov.justice.digital.hmpps.riskprofiler.config

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext
import com.microsoft.applicationinsights.web.internal.ThreadContext
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import uk.gov.justice.digital.hmpps.riskprofiler.utils.JwtAuthenticationHelper
import java.time.Duration
import java.util.UUID

@RunWith(SpringRunner::class)
@Import(JwtAuthenticationHelper::class, ClientTrackingTelemetryModule::class, JwtConfig::class)
@ActiveProfiles("test")
class ClientTrackingTelemetryModuleTest {
  @Autowired
  private lateinit var clientTrackingTelemetryModule: ClientTrackingTelemetryModule

  @Autowired
  private lateinit var jwtAuthenticationHelper: JwtAuthenticationHelper

  @Before
  fun setup() {
    ThreadContext.setRequestTelemetryContext(RequestTelemetryContext(1L))
  }

  @After
  fun tearDown() {
    ThreadContext.remove()
  }

  @Test
  fun shouldAddClientIdAndUserNameToInsightTelemetry() {
    val token = createJwt("bob", java.util.List.of(), 1L)
    val req = MockHttpServletRequest()
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    val res = MockHttpServletResponse()
    clientTrackingTelemetryModule.onBeginRequest(req, res)
    val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
    Assertions.assertThat(insightTelemetry).hasSize(2)
    Assertions.assertThat(insightTelemetry["username"]).isEqualTo("bob")
    Assertions.assertThat(insightTelemetry["clientId"]).isEqualTo("elite2apiclient")
  }

  @Test
  fun shouldAddOnlyClientIdIfUsernameNullToInsightTelemetry() {
    val token = createJwt(null, java.util.List.of(), 1L)
    val req = MockHttpServletRequest()
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    val res = MockHttpServletResponse()
    clientTrackingTelemetryModule.onBeginRequest(req, res)
    val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
    Assertions.assertThat(insightTelemetry).hasSize(1)
    Assertions.assertThat(insightTelemetry["clientId"]).isEqualTo("elite2apiclient")
  }

  @Test
  fun shouldNotAddClientIdAndUserNameToInsightTelemetryAsTokenExpired() {
    val token = createJwt("Fred", java.util.List.of(), -1L)
    val req = MockHttpServletRequest()
    req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
    val res = MockHttpServletResponse()
    clientTrackingTelemetryModule.onBeginRequest(req, res)
    val insightTelemetry = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
    Assertions.assertThat(insightTelemetry).hasSize(2)
    Assertions.assertThat(insightTelemetry["username"]).isEqualTo("Fred")
    Assertions.assertThat(insightTelemetry["clientId"]).isEqualTo("elite2apiclient")
  }

  private fun createJwt(user: String?, roles: List<String>, duration: Long): String {
    return jwtAuthenticationHelper.createJwt(
      user,
      java.util.List.of("read", "write"),
      roles,
      Duration.ofHours(duration),
      UUID.randomUUID().toString(),
    )
  }
}
