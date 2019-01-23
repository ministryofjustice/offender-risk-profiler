package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class DataRepository {

    private List<List<String>> ocgm = new ArrayList<>();
    private List<List<String>> pathfinder = new ArrayList<>();
    private List<List<String>> pras = new ArrayList<>();

    // Some comments here
    public void doHandleCsvData(List<List<String>> csvData, Exchange exchange)
    {
        var filename = exchange.getIn().getHeader("CamelFileName", String.class);
        log.info("Processing file {}", filename);

        String type = null;
        if (StringUtils.startsWithIgnoreCase(filename, "OCGM")) {
            this.ocgm = csvData;
            type = "OCGM";
        } else if (StringUtils.startsWithIgnoreCase(filename, "PATHFINDER")) {
            this.pathfinder = csvData;
            type = "PATHFINDER";
        } else if (StringUtils.startsWithIgnoreCase(filename, "PRAS")) {
            this.pras = csvData;
            type = "PRAS";
        }

        if (type != null) {
            log.info("Processed {} file", type);
        } else {
            log.warn("Unknown file type");
        }
    }

    public List<List<String>> getOcgmData() {
        return ocgm;
    }

    public List<List<String>> getPathfinderData() {
        return pathfinder;
    }

    public List<List<String>> getPrasData() {
        return pras;
    }
}
