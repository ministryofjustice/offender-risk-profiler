package uk.gov.justice.digital.hmpps.riskprofiler.queue

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class SpringCloudAwsConfig {
  @Bean
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "aws")
  @Primary
  fun awsSqsClient(
    @Value("\${sqs.aws.access.key.id}") accessKey: String?,
    @Value("\${sqs.aws.secret.access.key}") secretKey: String?
  ): AmazonSQSAsync {
    val creds = BasicAWSCredentials(accessKey, secretKey)
    return AmazonSQSAsyncClientBuilder.standard()
      .withCredentials(AWSStaticCredentialsProvider(creds))
      .build()
  }
}
