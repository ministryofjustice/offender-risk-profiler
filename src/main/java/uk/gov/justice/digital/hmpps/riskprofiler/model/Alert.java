package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.time.LocalDate;

@ApiModel(description = "Alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class Alert {
    private Long alertId;
    private Long bookingId;
    private String offenderNo;
    private String alertType;
    private String alertTypeDescription;
    private String alertCode;
    private String alertCodeDescription;
    private String comment;
    private LocalDate dateCreated;
    private LocalDate dateExpires;
    private boolean expired;
    private boolean active;
    private String addedByFirstName;
    private String addedByLastName;
    private String expiredByFirstName;
    private String expiredByLastName;
    private int ranking;
}