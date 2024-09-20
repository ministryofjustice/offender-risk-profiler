package uk.gov.justice.digital.hmpps.riskprofiler.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.time.Duration
import java.util.Date
import java.util.UUID

@Component
class JwtAuthenticationHelper(private val keyPair: KeyPair) {

  fun createJwt(
    subject: String?,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
  ): String =
    mutableMapOf<String, Any>()
      .also { subject?.let { subject -> it["user_name"] = subject } }
      .also { it["client_id"] = "elite2apiclient" }
      .also { roles?.let { roles -> it["authorities"] = roles } }
      .also { scope?.let { scope -> it["scope"] = scope } }
      .let {
        Jwts.builder()
          .setId(jwtId)
          .setSubject(subject)
          .addClaims(it.toMap())
          .setExpiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
          .signWith(SignatureAlgorithm.RS256, keyPair.private)
          .compact()
      }
}
