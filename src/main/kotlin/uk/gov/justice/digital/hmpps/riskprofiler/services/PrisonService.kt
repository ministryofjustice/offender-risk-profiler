package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrisonSupportedRepository
import uk.gov.justice.digital.hmpps.riskprofiler.exception.PrisonException.Companion.exists
import uk.gov.justice.digital.hmpps.riskprofiler.exception.PrisonException.Companion.withId
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PrisonService @Autowired constructor(
  private val repository: PrisonSupportedRepository,
  private val nomisService: NomisService,
) {
  val prisons: List<PrisonSupported>
    get() = repository.findAll()

  @Transactional
  fun addPrison(prisonId: String) {
    if (repository.existsById(prisonId)) {
      throw exists(prisonId)
    }
    val offendersAtPrison = nomisService.getOffendersAtPrison(prisonId)
    if (offendersAtPrison.isEmpty()) {
      throw withId(prisonId)
    }
    repository.save(PrisonSupported(prisonId, LocalDateTime.now()))
  }
}
