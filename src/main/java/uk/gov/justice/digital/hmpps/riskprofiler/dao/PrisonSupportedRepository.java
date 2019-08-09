package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported;

import java.util.List;

@Repository
public interface PrisonSupportedRepository extends CrudRepository<PrisonSupported, String> {

    List<PrisonSupported> findAll();
}
