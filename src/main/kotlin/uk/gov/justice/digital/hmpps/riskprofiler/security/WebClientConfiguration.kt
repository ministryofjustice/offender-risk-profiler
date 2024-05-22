package uk.gov.justice.digital.hmpps.riskprofiler.security

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfiguration @Autowired constructor(
  @Value("\${api.health-timeout:1s}") healthTimeout: Duration
) {
  @Value("\${elite2api.endpoint.url}")
  private lateinit var elite2apiRootUri: String

  @Value("\${pathfinderapi.endpoint.url}")
  private lateinit var pathfinderApiRootUri: String

  private val connector: ClientHttpConnector

  @Bean
  fun pathfinderApiHealthWebClient(): WebClient {
    return WebClient.builder().baseUrl(pathfinderApiRootUri).clientConnector(connector).build()
  }

  @Bean
  fun elite2ApiHealthWebClient(): WebClient {
    return WebClient.builder().baseUrl(elite2apiRootUri).clientConnector(connector).build()
  }

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager =
      AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun elite2SystemWebClient(authorizedClientManager: OAuth2AuthorizedClientManager?): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("api")
    return WebClient.builder()
      .exchangeStrategies(
        ExchangeStrategies.builder()
          .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024) }
          .build()
      )
      .baseUrl(elite2apiRootUri)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }

  @Bean
  fun pathfinderSystemWebClient(authorizedClientManager: OAuth2AuthorizedClientManager?): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("api")
    return WebClient.builder()
      .baseUrl(pathfinderApiRootUri)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }

  init {
    val httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, healthTimeout.toMillis().toInt())
      .doOnConnected { conn: Connection ->
        conn
          .addHandlerLast(ReadTimeoutHandler(healthTimeout.toSeconds(), TimeUnit.SECONDS))
          .addHandlerLast(WriteTimeoutHandler(healthTimeout.toSeconds(), TimeUnit.SECONDS))
      }
    connector = ReactorClientHttpConnector(httpClient)
  }
}