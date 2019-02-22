package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(of = {"questionnaireQueId", "questionnaireAnsId"})
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private String question;
    private String answer;
    private int questionSeq;
    private Long questionnaireQueId;
    private Long questionnaireAnsId;
    private LocalDateTime responseDate;
    private String responseCommentText;
    private Long recordStaffId;
}
