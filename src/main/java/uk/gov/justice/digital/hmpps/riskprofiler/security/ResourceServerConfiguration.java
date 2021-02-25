package uk.gov.justice.digital.hmpps.riskprofiler.security;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.gov.justice.digital.hmpps.riskprofiler.controllers.RiskProfilerResource;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

@Configuration
@EnableSwagger2
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableScheduling
@EnableSchedulerLock(defaultLockAtLeastFor = "PT10S", defaultLockAtMostFor = "PT12H")
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        http
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().headers().frameOptions().sameOrigin()
            // Can't have CSRF protection as requires session
            .and().csrf().disable()
            .authorizeRequests(auth -> {
                auth.antMatchers("/webjars/**",
                    "/favicon.ico",
                    "/csrf",
                    "/health/**",
                    "/info",
                    "/ping",
                    "/v2/api-docs",
                    "/swagger-ui.html", "/swagger-resources", "/swagger-resources/configuration/ui",
                    "/swagger-resources/configuration/security").permitAll()
                    .anyRequest()
                    .authenticated();
            })
            .oauth2ResourceServer().jwt().jwtAuthenticationConverter(new AuthAwareTokenConverter());
    }

    @Bean
    public Docket api() {

        ApiInfo apiInfo = new ApiInfo(
            "Offender Risk Profiler API Documentation",
            "API for accessing the Risk Profiles of Offenders.",
            getVersion(), "", contactInfo(), "", "",
            Collections.emptyList());

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .apiInfo(apiInfo)
            .select()
            .apis(RequestHandlerSelectors.basePackage(RiskProfilerResource.class.getPackage().getName()))
            .paths(PathSelectors.any())
            .build();

        docket.genericModelSubstitutes(Optional.class);
        docket.directModelSubstitute(ZonedDateTime.class, java.util.Date.class);
        docket.directModelSubstitute(LocalDateTime.class, java.util.Date.class);

        return docket;
    }

    /**
     * @return health data. Note this is unsecured so no sensitive data allowed!
     */
    private String getVersion() {
        return buildProperties == null ? "version not available" : buildProperties.getVersion();
    }

    private Contact contactInfo() {
        return new Contact(
            "HMPPS Digital Studio",
            "",
            "feedback@digital.justice.gov.uk");
    }

    @Bean
    @ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
    public AmazonS3 s3Client(@Value("${aws.access.key.id}") String accessKey, @Value("${aws.secret.access.key}") String secretKey, @Value("${aws.region}") String region) {
        var creds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(new AWSStaticCredentialsProvider(creds))
            .build();
    }

    @Bean
    @ConditionalOnProperty(name = "file.process.type", havingValue = "s3")
    public AmazonS3 viperS3Client(@Value("${viper.aws.access.key.id}") String accessKey, @Value("${viper.aws.secret.access.key}") String secretKey, @Value("${viper.aws.region}") String region) {
        var creds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(new AWSStaticCredentialsProvider(creds))
            .build();
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }
}
