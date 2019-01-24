package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OCGM;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.PathFinder;
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class DataRepository {

    private Map<String, OCGM> ocgm = new HashMap<>();
    private Map<String, PathFinder> pathfinder = new HashMap<>();
    private Map<String, Pras> pras = new HashMap<>();

    public void doHandleCsvData(List<List<String>> csvData, Exchange exchange){
        var filename = exchange.getIn().getHeader("CamelFileName", String.class);
        populateMap(csvData, filename);
    }
    // TODO In the following code there is the assumption that all the fields are there, data validation is needed
    // here or at some preprocessing step
    public void populateMap(List<List<String>> csvData, String filename)
    {
        log.info("Processing file {}", filename);

        String type = null;
        if (StringUtils.startsWithIgnoreCase(filename, "OCGM")) {

            Map<String, OCGM> map = new HashMap<>();
            for (List<String> p : csvData) {

                if (map.put(p.get(OCGM.getNomisIdPosition()),
                        OCGM.ocgmModelBuilder().
                        nomisId(p.get(OCGM.getNomisIdPosition())).
                        ocgmBand(p.get(OCGM.getOcgmBandPosition())).
                        standingWithinOcg(p.get(OCGM.getStandingPosition())).build()) != null) {
                    throw new IllegalStateException("Duplicate key found in OCGM Data");
                }
            }
            this.ocgm = map;
            type = "OCGM";

        } else if (StringUtils.startsWithIgnoreCase(filename, "PATHFINDER")) {
            Map<String, PathFinder> map = new HashMap<>();
            for (List<String> p : csvData) {
                if (map.put(p.get(PathFinder.getNomisIdPosition()),
                        PathFinder.pathFinderModelBuilder().nomisId(p.get(PathFinder.getNomisIdPosition())).
                                pathFinderBanding(p.get(PathFinder.getPathFinderBindingPosition())).build()) != null) {
                    throw new IllegalStateException("Duplicate key found in PathFinder");
                }
            }
            this.pathfinder = map;
            type = "PATHFINDER";
        } else if (StringUtils.startsWithIgnoreCase(filename, "PRAS")) {

            Map<String, Pras> map = new HashMap<>();
            for (List<String> p : csvData) {
                if (map.put(p.get(Pras.getNomisIdPosition()),
                        Pras.prasModelBuilder().nomisId(p.get(Pras.getNomisIdPosition())).build()) != null) {
                    throw new IllegalStateException("Duplicate key found in PRAS Data");
                }
            }
            pras = map;
            type = "PRAS";
        }

        if (type != null) {
            log.info("Processed {} file", type);
        } else {
            log.warn("Unknown file type");
        }
    }

    public Optional<OCGM> getOcgmDataByNomsId(String nomsId) {
        return Optional.ofNullable(ocgm.get(nomsId));
    }

    public Optional<PathFinder> getPathfinderDataByNomsId(String nomsId) {
        return Optional.ofNullable(pathfinder.get(nomsId));
    }

    public Optional<Pras> getPrasDataByNomsId(String nomsId) {
        return Optional.ofNullable(pras.get(nomsId));
    }
}
