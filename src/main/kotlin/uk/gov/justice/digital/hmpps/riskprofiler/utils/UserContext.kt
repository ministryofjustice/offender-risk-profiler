package uk.gov.justice.digital.hmpps.riskprofiler.utils

import org.springframework.stereotype.Component

@Component
object UserContext {
  private val authToken_ = ThreadLocal<String>()

  @JvmStatic
  var authToken: String?
    get() = authToken_.get()
    set(aToken) {
      authToken_.set(aToken)
    }
}
