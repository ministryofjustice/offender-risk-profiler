package uk.gov.justice.digital.hmpps.riskprofiler.security

import org.springframework.stereotype.Component

@Component
interface AuthenticationFacade {
  val currentUsername: String?
}
