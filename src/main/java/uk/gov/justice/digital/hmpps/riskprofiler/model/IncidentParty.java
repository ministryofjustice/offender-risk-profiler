package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class IncidentParty {

    private Long bookingId;
    private Long partySeq;
    private Long staffId;
    private Long personId;
    private String participationRole;
    private String outcomeCode;
    private String commentText;
    private Long incidentCaseId;

}
