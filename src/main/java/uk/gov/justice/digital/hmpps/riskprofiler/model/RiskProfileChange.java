package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class RiskProfileChange {
    private ProfileMessagePayload oldProfile;
    private ProfileMessagePayload newProfile;
    private String offenderNo;
    private LocalDateTime executeDateTime;
}