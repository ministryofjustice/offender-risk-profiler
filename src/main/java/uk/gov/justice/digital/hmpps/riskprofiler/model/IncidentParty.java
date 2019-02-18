package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@Data
@ToString
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
