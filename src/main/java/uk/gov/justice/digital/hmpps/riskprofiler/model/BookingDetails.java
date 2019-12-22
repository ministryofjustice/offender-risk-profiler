package uk.gov.justice.digital.hmpps.riskprofiler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(of = {"bookingId"})
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetails implements Serializable {

    private static final long serialVersionUID = 2L;

    private Long bookingId;
    private String offenderNo;
}
