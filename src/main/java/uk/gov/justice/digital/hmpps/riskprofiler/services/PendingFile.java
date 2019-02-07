package uk.gov.justice.digital.hmpps.riskprofiler.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingFile {
    private String fileName;
    private LocalDateTime fileTimestamp;
    private byte[] data;
}
