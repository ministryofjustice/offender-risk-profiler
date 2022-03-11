package uk.gov.justice.digital.hmpps.riskprofiler.queue

import com.amazon.sqs.javamessaging.ProviderConfiguration
import com.amazon.sqs.javamessaging.SQSConnectionFactory
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.support.destination.DynamicDestinationResolver
import javax.jms.Session

@Configuration
@EnableJms
@ConditionalOnProperty(name = ["sqs.provider"])
class JmsConfig {
  @Bean
  fun jmsListenerContainerFactory(@Qualifier("awsClientForEvents") awsSqs: AmazonSQS?): DefaultJmsListenerContainerFactory {
    val factory = DefaultJmsListenerContainerFactory()
    factory.setConnectionFactory(SQSConnectionFactory(ProviderConfiguration(), awsSqs))
    factory.setDestinationResolver(DynamicDestinationResolver())
    factory.setConcurrency("1-1")
    factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE)
    factory.setErrorHandler { t: Throwable? -> log.error("JMS error occurred", t) }
    return factory
  }

  @Bean
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "aws")
  fun awsClientForEvents(
    @Value("\${sqs.events.aws.access.key.id}") accessKey: String?,
    @Value("\${sqs.events.aws.secret.access.key}") secretKey: String?,
    @Value("\${cloud.aws.region.static}") region: String?
  ): AmazonSQSAsync {
    val creds = BasicAWSCredentials(accessKey, secretKey)
    return AmazonSQSAsyncClientBuilder.standard()
      .withCredentials(AWSStaticCredentialsProvider(creds))
      .withRegion(region)
      .build()
  }

  @Bean
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "aws")
  fun awsDlqClientForEvents(
    @Value("\${sqs.events.aws.dlq.access.key.id}") accessKey: String?,
    @Value("\${sqs.events.aws.dlq.secret.access.key}") secretKey: String?,
    @Value("\${cloud.aws.region.static}") region: String?
  ): AmazonSQSAsync {
    val creds = BasicAWSCredentials(accessKey, secretKey)
    return AmazonSQSAsyncClientBuilder.standard()
      .withCredentials(AWSStaticCredentialsProvider(creds))
      .withRegion(region)
      .build()
  }

  companion object {
    private val log = LoggerFactory.getLogger(JmsConfig::class.java)
  }
}
