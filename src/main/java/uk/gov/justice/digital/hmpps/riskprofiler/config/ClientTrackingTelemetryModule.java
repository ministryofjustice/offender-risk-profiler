package uk.gov.justice.digital.hmpps.riskprofiler.config;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.extensibility.TelemetryModule;
import com.microsoft.applicationinsights.web.extensibility.modules.WebTelemetryModule;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@Slf4j
@Configuration
public class ClientTrackingTelemetryModule implements WebTelemetryModule, TelemetryModule {

    @Override
    public void onBeginRequest(final ServletRequest req, final ServletResponse res) {

        final var httpServletRequest = (HttpServletRequest) req;
        final var token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final var bearer = "Bearer ";
        if (StringUtils.startsWithIgnoreCase(token, bearer)) {
            try {
                final var jwtBody = getClaimsFromJWT(StringUtils.substringAfter(token, bearer));
                final var properties = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

                properties.put("username", String.valueOf(jwtBody.getClaim("user_name")));
                properties.put("clientId", String.valueOf(jwtBody.getClaim("client_id")));
            } catch (final ParseException e) {
                // Parse token exception which spring security will handle
            }
        }
    }

    private JWTClaimsSet getClaimsFromJWT(final String token) throws ExpiredJwtException, ParseException {
        return SignedJWT.parse(token).getJWTClaimsSet();
    }

    @Override
    public void onEndRequest(final ServletRequest req, final ServletResponse res) {
    }

    @Override
    public void initialize(final TelemetryConfiguration configuration) {
    }
}
