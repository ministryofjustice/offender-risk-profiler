package uk.gov.justice.digital.hmpps.riskprofiler.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

import static uk.gov.justice.digital.hmpps.riskprofiler.utils.MdcUtility.CORRELATION_ID_HEADER;

@Slf4j
@Component
public class CorrelationMdcFilter implements Filter {

    private final MdcUtility mdcUtility;

    @Autowired
    public CorrelationMdcFilter(MdcUtility mdcUtility) {
        this.mdcUtility = mdcUtility;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialise - no functionality
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Optional<String> correlationIdOptional = Optional.ofNullable(((HttpServletRequest)request).getHeader(CORRELATION_ID_HEADER));

        try {
            MDC.put(CORRELATION_ID_HEADER, correlationIdOptional.orElseGet(mdcUtility::generateUUID));
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_HEADER);
        }
    }

    @Override
    public void destroy() {
        // destroy - no functionality
    }

}
