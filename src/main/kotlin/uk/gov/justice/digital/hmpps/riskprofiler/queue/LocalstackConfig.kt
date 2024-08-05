package uk.gov.justice.digital.hmpps.riskprofiler.queue

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class LocalstackConfig {
  @Bean("awsClientForEvents", "awsDlqClientForEvents", "awsSqsClient")
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "localstack")
  @Primary
  fun awsSqsClientLocalstack(
    @Value("\${sqs.events.endpoint.url}") serviceEndpoint: String?,
    @Value("\${cloud.aws.region.static}") region: String?,
    @Value("\${s3.aws.access.key.id}") accessKey: String,
    @Value("\${s3.aws.secret.access.key}") secretKey: String,
  ): AmazonSQSAsync {
    return AmazonSQSAsyncClientBuilder.standard()
      .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey)))
      .withEndpointConfiguration(EndpointConfiguration(serviceEndpoint, region))
      .build()
  }

  @Bean("s3Client")
  @ConditionalOnProperty(name = ["s3.provider"], havingValue = "localstack")
  open fun awsS3ClientLocalstack(
    @Value("\${s3.aws.access.key.id}") accessKey: String,
    @Value("\${s3.aws.secret.access.key}") secretKey: String,
    @Value("\${s3.endpoint.region}") region: String,
    //   @Autowired localStackContainer: LocalStackContainer,
  ): AmazonS3 {
    return AmazonS3ClientBuilder.standard()
      .withPathStyleAccessEnabled(true)
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:4566", region))
      // Cannot supply anonymous credentials here since only a subset of S3 APIs will accept unsigned requests
      .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey)))
      .build()
  }
}
