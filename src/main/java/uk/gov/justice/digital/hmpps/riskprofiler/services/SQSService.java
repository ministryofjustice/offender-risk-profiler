package uk.gov.justice.digital.hmpps.riskprofiler.services;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessageChannel;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfileChange;

@Service
public class SQSService {

    private final QueueMessagingTemplate queueTemplate;
    private final AmazonSQSAsync amazonSqs;
    private final String queueUrl;

    public SQSService(@Qualifier("awsSqsClient") AmazonSQSAsync amazonSqs,
                      @Value("${sqs.rpc.queue.url}") String queueUrl) {

        this.queueTemplate = new QueueMessagingTemplate(amazonSqs);
        this.queueUrl = queueUrl;
        this.amazonSqs = amazonSqs;
    }

    void sendRiskProfileChangeMessage(RiskProfileChange payload) {
        queueTemplate.convertAndSend(new QueueMessageChannel(amazonSqs,queueUrl), payload);
    }
}
