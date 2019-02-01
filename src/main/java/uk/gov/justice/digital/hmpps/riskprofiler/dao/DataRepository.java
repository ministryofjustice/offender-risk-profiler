package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class DataRepository {

    private Map<String, Ocgm> ocgm = new HashMap<>();
    private Map<String, PathFinder> pathfinder = new HashMap<>();
    private Map<String, Pras> pras = new HashMap<>();

    public void doHandleCsvData(List<List<String>> csvData, Exchange exchange) {
        var filename = exchange.getIn().getHeader("CamelFileName", String.class);
        populateMap(csvData, filename);
    }

    // TODO In the following code there is the assumption that all the fields are there, data validation is needed
    // here or at some preprocessing step
    public void populateMap(List<List<String>> csvData, String filename) {
        log.info("Processing file {}", filename);

        String type = null;
        if (StringUtils.startsWithIgnoreCase(filename, "Ocgm")) {

            var map = new HashMap<String, Ocgm>();
            csvData.forEach(p -> {
                var ocgmLine = Ocgm.builder()
                        .nomisId(p.get(Ocgm.NOMIS_ID_POSITION))
                        .ocgmBand(p.get(Ocgm.OCGM_BAND_POSITION))
                        .standingWithinOcg(p.get(Ocgm.STANDING_POSITION))
                        .build();

                if (map.put(ocgmLine.getNomisId(), ocgmLine) != null) {
                    log.warn("Duplicate key found in OCGM Data {}", p);
                }
            });

            this.ocgm = map;
            type = "Ocgm";

        } else if (StringUtils.startsWithIgnoreCase(filename, "PATHFINDER")) {
            var map = new HashMap<String, PathFinder>();
            csvData.forEach(p -> {
                var pathFinderLine = PathFinder.builder()
                        .nomisId(p.get(PathFinder.NOMIS_ID_POSITION))
                        .pathFinderBanding(p.get(PathFinder.PATH_FINDER_BINDING_POSITION))
                        .build();

                if (map.put(pathFinderLine.getNomisId(), pathFinderLine) != null) {
                    log.warn("Duplicate key found in PathFinder {}", p);
                }
            });

            this.pathfinder = map;
            type = "PATHFINDER";
        } else if (StringUtils.startsWithIgnoreCase(filename, "PRAS")) {

            var map = new HashMap<String, Pras>();
            csvData.forEach(p -> {
                var prasLine = Pras.builder().nomisId(p.get(Pras.NOMIS_ID_POSITION)).build();

                if (map.put(prasLine.getNomisId(), prasLine) != null) {
                    log.warn("Duplicate key found in PRAS Data {}", p);
                }
            });
            pras = map;
            type = "PRAS";
        }

        if (type != null) {
            log.info("Processed {} file", type);
        } else {
            log.warn("Unknown file type");
        }
    }

    public Optional<Ocgm> getOcgmDataByNomsId(String nomsId) {
        return Optional.ofNullable(ocgm.get(nomsId));
    }

    public Optional<PathFinder> getPathfinderDataByNomsId(String nomsId) {
        return Optional.ofNullable(pathfinder.get(nomsId));
    }

    public Optional<Pras> getPrasDataByNomsId(String nomsId) {
        return Optional.ofNullable(pras.get(nomsId));
    }
}
