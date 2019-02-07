package uk.gov.justice.digital.hmpps.riskprofiler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private String alertCode;
    private String dateExpires;
    private boolean expired;
    private boolean active;
    private int ranking;
}
