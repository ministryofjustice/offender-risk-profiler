package uk.gov.justice.digital.hmpps.riskprofiler.config

import com.microsoft.applicationinsights.TelemetryConfiguration
import com.microsoft.applicationinsights.extensibility.TelemetryModule
import com.microsoft.applicationinsights.web.extensibility.modules.WebTelemetryModule
import com.microsoft.applicationinsights.web.internal.ThreadContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.jsonwebtoken.ExpiredJwtException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import java.text.ParseException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

@Configuration
class ClientTrackingTelemetryModule : WebTelemetryModule, TelemetryModule {
  override fun onBeginRequest(req: ServletRequest, res: ServletResponse) {
    val httpServletRequest = req as HttpServletRequest
    val token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)
    val bearer = "Bearer "
    if (StringUtils.startsWithIgnoreCase(token, bearer)) {
      try {
        val jwtBody = getClaimsFromJWT(StringUtils.substringAfter(token, bearer))
        val properties = ThreadContext.getRequestTelemetryContext().httpRequestTelemetry.properties
        val user = jwtBody.getStringClaim("user_name")
        if (user != null) {
          properties["username"] = user
        }
        val client = jwtBody.getStringClaim("client_id")
        if (client != null) {
          properties["clientId"] = client
        }
      } catch (e: ParseException) {
        // Parse token exception which spring security will handle
      }
    }
  }

  @Throws(ExpiredJwtException::class, ParseException::class)
  private fun getClaimsFromJWT(token: String): JWTClaimsSet {
    return SignedJWT.parse(token).jwtClaimsSet
  }

  override fun onEndRequest(req: ServletRequest, res: ServletResponse) {}
  override fun initialize(configuration: TelemetryConfiguration) {}

  companion object {
    private val log = LoggerFactory.getLogger(ClientTrackingTelemetryModule::class.java)
  }
}
