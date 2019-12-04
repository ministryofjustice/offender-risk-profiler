package uk.gov.justice.digital.hmpps.riskprofiler.queue;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.services.sqs.AmazonSQS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.Session;

@Configuration
@EnableJms
@ConditionalOnProperty(name = "sqs.provider")
@Slf4j
public class JmsConfig {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(final AmazonSQS awsSqs) {
        final var factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(new SQSConnectionFactory(new ProviderConfiguration(), awsSqs));
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("2-10");
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setErrorHandler(t -> log.error("JMS error occurred", t));
        return factory;
    }
}
