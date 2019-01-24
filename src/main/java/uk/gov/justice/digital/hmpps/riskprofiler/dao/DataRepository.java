package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class DataRepository {

    private Map<String, List<String>> ocgm = new HashMap<>();
    private Map<String, List<String>> pathfinder = new HashMap<>();
    private Map<String, List<String>> pras = new HashMap<>();

    // Some comments here
    public void doHandleCsvData(List<List<String>> csvData, Exchange exchange)
    {
        var filename = exchange.getIn().getHeader("CamelFileName", String.class);
        log.info("Processing file {}", filename);

        String type = null;
        if (StringUtils.startsWithIgnoreCase(filename, "OCGM")) {
            this.ocgm = csvData.stream()
                    .collect(Collectors.toMap(p -> p.get(0),
                            Function.identity()));
            type = "OCGM";
        } else if (StringUtils.startsWithIgnoreCase(filename, "PATHFINDER")) {
            this.pathfinder = csvData.stream()
                    .collect(Collectors.toMap(p -> p.get(3),
                            Function.identity()));
            type = "PATHFINDER";
        } else if (StringUtils.startsWithIgnoreCase(filename, "PRAS")) {

            pras = csvData.stream()
                    .collect(Collectors.toMap(p -> p.get(11),
                            Function.identity()));
            type = "PRAS";
        }

        if (type != null) {
            log.info("Processed {} file", type);
        } else {
            log.warn("Unknown file type");
        }
    }

    public Optional<List<String>> getOcgmDataByNomsId(String nomsId) {
        return Optional.ofNullable(ocgm.get(nomsId));
    }

    public Optional<List<String>> getPathfinderDataByNomsId(String nomsId) {
        return Optional.ofNullable(pathfinder.get(nomsId));
    }

    public Optional<List<String>> getPrasDataByNomsId(String nomsId) {
        return Optional.ofNullable(pras.get(nomsId));
    }
}
