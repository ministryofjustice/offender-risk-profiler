package uk.gov.justice.digital.hmpps.riskprofiler.model;

import lombok.*;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ProfileMessagePayload {
    private EscapeProfile escape;
    private ExtremismProfile extremism;
    private SocProfile soc;
    private ViolenceProfile violence;
}
