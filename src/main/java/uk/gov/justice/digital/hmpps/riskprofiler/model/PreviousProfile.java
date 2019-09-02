package uk.gov.justice.digital.hmpps.riskprofiler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "PREVIOUS_PROFILE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PreviousProfile {
    @Id
    @Length(max = 10)
    private String offenderNo;

    private String escape;
    private String extremism;
    private String soc;
    private String violence;

    @NotNull
    private LocalDateTime executeDateTime;
}
