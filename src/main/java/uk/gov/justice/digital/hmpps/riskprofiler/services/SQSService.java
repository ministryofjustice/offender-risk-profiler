package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.core.QueueMessageChannel;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfileChange;

@Service
public class SQSService {

    private final QueueMessagingTemplate queueTemplate;
    private final AmazonSQSAsync amazonSqs;
    private final String queueUrl;

    public SQSService(@Qualifier("awsSqsClient") AmazonSQSAsync amazonSqs,
                      @Value("${sqs.rpc.queue.url}") String queueUrl) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new StdDateFormat());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setSerializedPayloadClass(String.class);
        converter.setObjectMapper(mapper);

        this.queueTemplate = new QueueMessagingTemplate(amazonSqs, (ResourceIdResolver) null, converter);
        this.queueUrl = queueUrl;
        this.amazonSqs = amazonSqs;
    }

    void sendRiskProfileChangeMessage(RiskProfileChange payload) {
        queueTemplate.convertAndSend(new QueueMessageChannel(amazonSqs,queueUrl), payload);
    }
}
