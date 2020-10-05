package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@EqualsAndHashCode(of = {"nomisId"})
@ToString
public class PathFinder {

    private String nomisId;
    private Integer pathFinderBanding;
}
