package uk.gov.justice.digital.hmpps.riskprofiler.queue

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder
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
class SpringCloudAwsConfig {
  @Bean
  @ConditionalOnProperty(name = ["sqs.provider"], havingValue = "aws")
  @Primary
  fun awsSqsClient(
    @Value("\${sqs.aws.region}") region: String,
  ): AmazonSQSAsync {
    return AmazonSQSAsyncClientBuilder.standard()
      .withCredentials(DefaultAWSCredentialsProviderChain())
      .withRegion(region)
      .build()
  }

  @Bean("awsSqsClient")
  @ConditionalOnProperty(name = ["s3.provider"], havingValue = "localstack")
  fun awsS3ClientLocalstack(
    @Value("\${sqs.aws.access.key.id}") accessKey: String,
    @Value("\${sqs.aws.secret.access.key}") secretKey: String,
    @Value("\${sqs.events.endpoint.url}") serviceEndpoint: String,
    @Value("\${sqs.endpoint.region}") region: String,
  ): AmazonS3 {
    return AmazonS3ClientBuilder.standard()
      .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(serviceEndpoint, region))
      // Cannot supply anonymous credentials here since only a subset of S3 APIs will accept unsigned requests
      .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey)))
      .build()
  }
}
