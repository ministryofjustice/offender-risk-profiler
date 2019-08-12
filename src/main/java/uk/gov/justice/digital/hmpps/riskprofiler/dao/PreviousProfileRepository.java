package uk.gov.justice.digital.hmpps.riskprofiler.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.hmpps.riskprofiler.model.PreviousProfile;

@Repository
public interface PreviousProfileRepository extends CrudRepository<PreviousProfile, String> {
}
