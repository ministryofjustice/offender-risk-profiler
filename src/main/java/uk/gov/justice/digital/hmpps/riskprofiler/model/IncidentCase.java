package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(of = {"incidentCaseId"})
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCase implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long incidentCaseId;
    private String incidentTitle;
    private String incidentType;
    private String incidentDetails;
    private LocalDate incidentDate;
    private LocalDateTime incidentTime;
    private Long reportedStaffId;
    private LocalDate reportDate;
    private LocalDateTime reportTime;
    private String incidentStatus;
    private String agencyId;
    private Boolean responseLockedFlag;
    private List<IncidentResponse> responses;
    private List<IncidentParty> parties;
}
