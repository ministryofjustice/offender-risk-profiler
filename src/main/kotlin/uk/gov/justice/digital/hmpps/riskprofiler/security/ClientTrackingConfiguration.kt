package uk.gov.justice.digital.hmpps.riskprofiler.security

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.text.ParseException
import java.util.Optional

@Configuration
@ConditionalOnExpression("T(org.apache.commons.lang3.StringUtils).isNotBlank('\${applicationinsights.connection.string:}')")
class ClientTrackingConfiguration(private val clientTrackingInterceptor: ClientTrackingInterceptor) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**")
  }
}

@Configuration
class ClientTrackingInterceptor : HandlerInterceptor {
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    val token = request.getHeader(HttpHeaders.AUTHORIZATION)
    val bearer = "Bearer "
    if (StringUtils.startsWithIgnoreCase(token, bearer)) {
      try {
        val jwtBody = getClaimsFromJWT(token)
        val user = Optional.ofNullable(jwtBody.getClaim("user_name"))
        user.map { it.toString() }.ifPresent {
          Span.current().setAttribute("username", it) // username in customDimensions
          Span.current().setAttribute("enduser.id", it) // user_Id at the top level of the request
        }
        Span.current().setAttribute("clientId", jwtBody.getClaim("client_id").toString())
      } catch (e: ParseException) {
        log.warn("problem decoding jwt public key for application insights", e)
      }
    }
    return true
  }

  @Throws(ParseException::class)
  private fun getClaimsFromJWT(token: String): JWTClaimsSet =
    SignedJWT.parse(token.replace("Bearer ", "")).jwtClaimsSet

  companion object {
    private val log = LoggerFactory.getLogger(ClientTrackingInterceptor::class.java)
  }
}
