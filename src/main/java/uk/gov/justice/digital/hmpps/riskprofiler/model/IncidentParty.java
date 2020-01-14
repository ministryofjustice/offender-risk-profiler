package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentParty implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long bookingId;
    private Long partySeq;
    private Long staffId;
    private Long personId;
    private String participationRole;
    private String outcomeCode;
    private String commentText;
    private Long incidentCaseId;
}
