package uk.gov.justice.digital.hmpps.riskprofiler.security

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
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
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import uk.gov.justice.digital.hmpps.riskprofiler.controllers.RiskProfilerResource
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Date
import java.util.Optional
import javax.sql.DataSource

@Configuration
@EnableSwagger2
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
          "/v2/api-docs",
          "/swagger-ui.html",
          "/swagger-ui/**",
          "/swagger-resources/**"
        ).permitAll().anyRequest().authenticated()
      }.oauth2ResourceServer().jwt().jwtAuthenticationConverter(AuthAwareTokenConverter())
  }

  @Bean
  fun api(): Docket {
    val apiInfo = ApiInfo(
      "Offender Risk Profiler API Documentation",
      "API for accessing the Risk Profiles of Offenders.",
      version, "", contactInfo(), "", "", emptyList()
    )
    val docket = Docket(DocumentationType.SWAGGER_2)
      .useDefaultResponseMessages(false)
      .apiInfo(apiInfo)
      .select()
      .apis(RequestHandlerSelectors.basePackage(RiskProfilerResource::class.java.getPackage().name))
      .paths(PathSelectors.any())
      .build()
    docket.genericModelSubstitutes(Optional::class.java)
    docket.directModelSubstitute(ZonedDateTime::class.java, Date::class.java)
    docket.directModelSubstitute(LocalDateTime::class.java, Date::class.java)
    return docket
  }

  /**
   * @return health data. Note this is unsecured so no sensitive data allowed!
   */
  private val version: String
    get() = if (buildProperties == null) "version not available" else buildProperties.version

  private fun contactInfo(): Contact {
    return Contact(
      "HMPPS Digital Studio",
      "",
      "feedback@digital.justice.gov.uk"
    )
  }

  @Bean
  @ConditionalOnProperty(name = ["file.process.type"], havingValue = "s3")
  fun s3Client(
    @Value("\${aws.access.key.id}") accessKey: String?,
    @Value("\${aws.secret.access.key}") secretKey: String?,
    @Value("\${aws.region}") region: String?
  ): AmazonS3 {
    val creds = BasicAWSCredentials(accessKey, secretKey)
    return AmazonS3ClientBuilder.standard()
      .withRegion(region)
      .withCredentials(AWSStaticCredentialsProvider(creds))
      .build()
  }

  @Bean
  @ConditionalOnProperty(name = ["file.process.type"], havingValue = "s3")
  fun viperS3Client(
    @Value("\${viper.aws.access.key.id}") accessKey: String?,
    @Value("\${viper.aws.secret.access.key}") secretKey: String?,
    @Value("\${viper.aws.region}") region: String?
  ): AmazonS3 {
    val creds = BasicAWSCredentials(accessKey, secretKey)
    return AmazonS3ClientBuilder.standard()
      .withRegion(region)
      .withCredentials(AWSStaticCredentialsProvider(creds))
      .build()
  }

  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider {
    return JdbcTemplateLockProvider(dataSource)
  }
}
