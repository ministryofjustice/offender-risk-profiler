package uk.gov.justice.digital.hmpps.riskprofiler.security

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import javax.sql.DataSource

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableScheduling
@EnableSchedulerLock(defaultLockAtLeastFor = "PT10S", defaultLockAtMostFor = "PT12H")
class ResourceServerConfiguration : WebSecurityConfigurerAdapter() {
  @Autowired(required = false)
  private val buildProperties: BuildProperties? = null

  @Throws(Exception::class)
  public override fun configure(http: HttpSecurity) {
    http
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and().headers().frameOptions().sameOrigin() // Can't have CSRF protection as requires session
      .and().csrf().disable()
      .authorizeRequests { auth ->
        auth.antMatchers(
          "/webjars/**",
          "/favicon.ico",
          "/csrf",
          "/health/**",
          "/info",
          "/ping",
          "/h2-console/**",
          "/v3/api-docs",
          "/swagger-ui.html",
          "/swagger-ui/**",
          "/swagger-resources/**"
        ).permitAll().anyRequest().authenticated()
      }.oauth2ResourceServer().jwt().jwtAuthenticationConverter(AuthAwareTokenConverter())
  }

  @Bean
  fun api(): OpenAPI {

    return OpenAPI()
      .components(
        Components().addSecuritySchemes(
          "bearer-jwt",
          SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")
        )
      )
      .info(
        Info().title("Offender Risk Profiler API Documentation")
          .description("API for accessing the Risk Profiles of Offenders")
          .version(buildProperties?.version)
          .license(
            License()
              .name("Open Government Licence v3.0")
              .url("https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/")
          )
          .contact(Contact().name("HMPPS Digital Studio").email("dps-hmpps@digital.justice.gov.uk"))
      )
      .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))
  }

  /**
   * @return health data. Note this is unsecured so no sensitive data allowed!
   */
  private val version: String
    get() = if (buildProperties == null) "version not available" else buildProperties.version

  @Bean
  @ConditionalOnProperty(name = ["s3.provider"], havingValue = "aws")
  fun s3Client(
    @Value("\${s3.aws.access.key.id}") accessKey: String?,
    @Value("\${s3.aws.secret.access.key}") secretKey: String?,
    @Value("\${s3.endpoint.region}") region: String?
  ): AmazonS3 {
    val creds = BasicAWSCredentials(accessKey, secretKey)
    return AmazonS3ClientBuilder.standard()
      .withRegion(region)
      .withCredentials(AWSStaticCredentialsProvider(creds))
      .build()
  }

  @Bean("s3Client")
  @ConditionalOnProperty(name = ["s3.provider"], havingValue = "localstack")
  open fun awsS3ClientLocalstack(
    @Value("\${s3.aws.access.key.id}") accessKey: String,
    @Value("\${s3.aws.secret.access.key}") secretKey: String,
    @Value("\${s3.endpoint.region}") region: String
  ): AmazonS3 {
    return AmazonS3ClientBuilder.standard()
      .withPathStyleAccessEnabled(true)
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:4572", region))
      // Cannot supply anonymous credentials here since only a subset of S3 APIs will accept unsigned requests
      .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey)))
      .build()
  }


  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider {
    return JdbcTemplateLockProvider(dataSource)
  }
}
