package uk.gov.justice.digital.hmpps.riskprofiler.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.hmpps.riskprofiler.utils.JwtAuthInterceptor;

import java.time.Duration;
import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    private final OAuth2ClientContext oauth2ClientContext;
    private final ClientCredentialsResourceDetails elite2apiDetails;

    @Value("${elite2.api.uri.root}")
    private String apiRootUri;

    @Value("${elite2api.endpoint.url}")
    private String elite2apiRootUri;

    @Value("${pathfinderapi.endpoint.url}")
    private String pathfinderApiRootUri;

    private final Duration healthTimeout;

    @Autowired
    public RestTemplateConfiguration(
            OAuth2ClientContext oauth2ClientContext,
            ClientCredentialsResourceDetails elite2apiDetails,
            @Value("${api.health-timeout:1s}") Duration healthTimeout) {
        this.oauth2ClientContext = oauth2ClientContext;
        this.elite2apiDetails = elite2apiDetails;
        this.healthTimeout = healthTimeout;
    }

    @Bean(name = "pathfinderApiHealthRestTemplate")
    public RestTemplate pathfinderApiHealthRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(pathfinderApiRootUri)
                .setConnectTimeout(healthTimeout)
                .setReadTimeout(healthTimeout)
                .build();
    }

    @Bean(name = "elite2ApiHealthRestTemplate")
    public RestTemplate elite2ApiHealthRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(elite2apiRootUri)
                .setConnectTimeout(healthTimeout)
                .setReadTimeout(healthTimeout)
                .build();
    }

    @Bean(name = "elite2SystemRestTemplate")
    public OAuth2RestTemplate elite2SystemRestTemplate(GatewayAwareAccessTokenProvider accessTokenProvider) {

        OAuth2RestTemplate elite2SystemRestTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContext);

        elite2SystemRestTemplate.setAccessTokenProvider(accessTokenProvider);

        RootUriTemplateHandler.addTo(elite2SystemRestTemplate, this.apiRootUri);
        return elite2SystemRestTemplate;
    }

    @Bean(name = "pathfinderSystemRestTemplate")
    public OAuth2RestTemplate pathfinderSystemRestTemplate(GatewayAwareAccessTokenProvider accessTokenProvider) {

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContext);

        restTemplate.setAccessTokenProvider(accessTokenProvider);

        RootUriTemplateHandler.addTo(restTemplate, this.pathfinderApiRootUri);
        return restTemplate;
    }

    /**
     * This subclass is necessary to make OAuth2AccessTokenSupport.getRestTemplate() public
     */
    @Component("accessTokenProvider")
    public class GatewayAwareAccessTokenProvider extends ClientCredentialsAccessTokenProvider {
        @Override
        public RestOperations getRestTemplate() {
            return super.getRestTemplate();
        }
    }
}
