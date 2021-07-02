package uk.gov.justice.digital.hmpps.riskprofiler.utils

import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.riskprofiler.security.UserSecurityUtils
import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
@Order(1)
class UserMdcFilter @Autowired constructor(private val userSecurityUtils: UserSecurityUtils) : Filter {
  override fun init(filterConfig: FilterConfig) {
    // Initialise - no functionality
  }

  @Throws(IOException::class, ServletException::class)
  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val currentUsername = userSecurityUtils.currentUsername
    try {
      if (currentUsername != null) {
        MDC.put(MdcUtility.USER_ID_HEADER, currentUsername)
      }
      chain.doFilter(request, response)
    } finally {
      if (currentUsername != null) {
        MDC.remove(MdcUtility.USER_ID_HEADER)
      }
    }
  }

  override fun destroy() {
    // Destroy - no functionality
  }
}
