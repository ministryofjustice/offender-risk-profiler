package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ImportedFile<RiskDataSet> {
    private String fileName;
    private LocalDateTime fileTimestamp;
    private Map<String, RiskDataSet> dataSet;

}
