package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ImportedFile<RiskDataSet> {
    private String fileName;
    private LocalDateTime fileTimestamp;
    private Map<String, RiskDataSet> dataSet;

    @Builder.Default
    private AtomicInteger index = new AtomicInteger();
    @Builder.Default
    private AtomicInteger linesProcessed = new AtomicInteger();
    @Builder.Default
    private AtomicInteger linesDup = new AtomicInteger();
    @Builder.Default
    private AtomicInteger linesError = new AtomicInteger();
    @Builder.Default
    private AtomicInteger linesInvalid = new AtomicInteger();

    public void reset() {
        dataSet = new HashMap<>();
        index.set(0);
        linesProcessed.set(0);
        linesDup.set(0);
        linesError.set(0);
        linesInvalid.set(0);
    }

}
