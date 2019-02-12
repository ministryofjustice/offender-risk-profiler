package uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(of = { "nomisId" })
@ToString
public class OcgmList implements RiskDataSet {

    private final String nomisId;
    private final List<Ocgm> data;

    @Override
    public String getKey() {
        return nomisId;
    }

    @Builder
    public OcgmList(String nomisId, Ocgm ocgm, List<Ocgm> ocgms) {
        this.nomisId = nomisId;
        this.data = new ArrayList<>();
        if (ocgm != null) {
            this.data.add(ocgm);
        } else if (ocgms != null) {
            this.data.addAll(ocgms);
        }
    }


}
