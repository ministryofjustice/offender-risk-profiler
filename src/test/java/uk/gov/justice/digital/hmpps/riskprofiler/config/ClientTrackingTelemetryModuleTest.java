package uk.gov.justice.digital.hmpps.riskprofiler.config;

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.hmpps.riskprofiler.utils.JwtAuthenticationHelper;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import({JwtAuthenticationHelper.class, ClientTrackingTelemetryModule.class, JwtConfig.class})
@ActiveProfiles("test")
public class ClientTrackingTelemetryModuleTest {

    @Autowired
    private ClientTrackingTelemetryModule clientTrackingTelemetryModule;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JwtAuthenticationHelper jwtAuthenticationHelper;

    @Before
    public void setup() {
        ThreadContext.setRequestTelemetryContext(new RequestTelemetryContext(1L));
    }

    @After
    public void tearDown() {
        ThreadContext.remove();
    }

    @Test
    public void shouldAddClientIdAndUserNameToInsightTelemetry() {

        final var token = createJwt("bob", List.of(), 1L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        clientTrackingTelemetryModule.onBeginRequest(req, res);

        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

        assertThat(insightTelemetry).hasSize(2);
        assertThat(insightTelemetry.get("username")).isEqualTo("bob");
        assertThat(insightTelemetry.get("clientId")).isEqualTo("elite2apiclient");

    }

    @Test
    public void shouldAddOnlyClientIdIfUsernameNullToInsightTelemetry() {

        final var token = createJwt(null, List.of(), 1L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        clientTrackingTelemetryModule.onBeginRequest(req, res);

        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

        assertThat(insightTelemetry).hasSize(2);
        assertThat(insightTelemetry.get("username")).isEqualTo("null");
        assertThat(insightTelemetry.get("clientId")).isEqualTo("elite2apiclient");
    }

    @Test
    public void shouldNotAddClientIdAndUserNameToInsightTelemetryAsTokenExpired() {

        final var token = createJwt("Fred", List.of(), -1L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        clientTrackingTelemetryModule.onBeginRequest(req, res);

        final var insightTelemetry = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

        assertThat(insightTelemetry).hasSize(2);
        assertThat(insightTelemetry.get("username")).isEqualTo("Fred");
        assertThat(insightTelemetry.get("clientId")).isEqualTo("elite2apiclient");
    }

    private String createJwt(final String user, final List<String> roles, Long duration) {
        return jwtAuthenticationHelper.createJwt(user,
                List.of("read", "write"), roles, Duration.ofHours(duration), UUID.randomUUID().toString());
    }

}
