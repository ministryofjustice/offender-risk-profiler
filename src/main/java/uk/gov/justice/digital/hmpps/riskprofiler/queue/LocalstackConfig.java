package uk.gov.justice.digital.hmpps.riskprofiler.queue;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LocalstackConfig {

    @Bean({"awsClientForEvents", "awsDlqClientForEvents", "awsSqsClient"})
    @ConditionalOnProperty(name = "sqs.provider", havingValue = "localstack")
    @Primary
    public AmazonSQSAsync awsSqsClient(
            @Value("${sqs.events.endpoint.url}") final String serviceEndpoint,
            @Value("${cloud.aws.region.static}") final String region
    ) {
        final var creds = new AnonymousAWSCredentials();
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, region))
                .build();
    }
}
