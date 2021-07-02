package uk.gov.justice.digital.hmpps.riskprofiler.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class UserSecurityUtils : AuthenticationFacade {
  val authentication: Authentication
    get() = SecurityContextHolder.getContext().authentication
  override val currentUsername: String?
    get() {
      val username: String?
      val userPrincipal = userPrincipal
      username = if (userPrincipal is String) {
        userPrincipal
      } else if (userPrincipal is UserDetails) {
        userPrincipal.username
      } else if (userPrincipal is Map<*, *>) {
        userPrincipal["username"] as String?
      } else {
        null
      }
      return username
    }
  private val userPrincipal: Any?
    get() {
      val auth = authentication
      return if (auth != null) auth.principal else null
    }
}
