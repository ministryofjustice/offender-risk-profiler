package uk.gov.justice.digital.hmpps.riskprofiler.security;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfiguration {

    @Value("${elite2api.endpoint.url}")
    private String elite2apiRootUri;

    @Value("${pathfinderapi.endpoint.url}")
    private String pathfinderApiRootUri;

    private final ClientHttpConnector connector;

    @Autowired
    public WebClientConfiguration(
            @Value("${api.health-timeout:1s}") final Duration healthTimeout) {
        final HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) healthTimeout.toMillis())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(healthTimeout.toSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(healthTimeout.toSeconds(), TimeUnit.SECONDS))
                );
        connector = new ReactorClientHttpConnector(httpClient);
    }

    @Bean
    WebClient pathfinderApiHealthWebClient() {
        return WebClient.builder().baseUrl(pathfinderApiRootUri).clientConnector(connector).build();
    }

    @Bean
    WebClient elite2ApiHealthWebClient() {
        return WebClient.builder().baseUrl(elite2apiRootUri).clientConnector(connector).build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientRepository authorizedClientRepository) {

        final OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();

        final DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    WebClient elite2SystemWebClient(final OAuth2AuthorizedClientManager authorizedClientManager) {
        final ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("api");
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                        .build())
                .baseUrl(elite2apiRootUri)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    @Bean
    WebClient pathfinderSystemWebClient(final OAuth2AuthorizedClientManager authorizedClientManager) {
        final ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("api");
        return WebClient.builder()
                .baseUrl(pathfinderApiRootUri)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }
}
