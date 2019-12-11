package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OffenderAlertEvent {
    private String eventType;
    private LocalDateTime eventDatetime;
    private Long bookingId;
    private String alertType;
    private String alertCode;
}

