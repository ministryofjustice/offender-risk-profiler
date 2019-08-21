package uk.gov.justice.digital.hmpps.riskprofiler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProfileMessagePayload {
    private String offenderNo;
    private EscapeProfile escape;
    private ExtremismProfile extremism;
    private SocProfile soc;
    private ViolenceProfile violence;
    private LocalDateTime executeDateTime;
    private Status status;
}
