package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class OffenderEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime eventDatetime;
    private Long rootOffenderId;
    private Long bookingId;
    private Long alertSeq;
    private LocalDateTime alertDateTime;
    private String alertType;
    private String alertCode;
    private LocalDateTime expiryDateTime;

    private String nomisEventType;
}

