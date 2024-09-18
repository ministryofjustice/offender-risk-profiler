package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrisonSupportedRepository
import uk.gov.justice.digital.hmpps.riskprofiler.exception.PrisonException
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported
import java.util.Arrays

@RunWith(MockitoJUnitRunner::class)
class PrisonServiceTest {
  @Mock
  private lateinit var repository: PrisonSupportedRepository

  @Mock
  private lateinit var nomisService: NomisService

  private lateinit var prisonService: PrisonService

  @Before
  fun init() {
    prisonService = PrisonService(repository, nomisService)
  }

  @Test
  fun addPrisonHappy() {
    Mockito.`when`(repository.existsById(PRISON)).thenReturn(false)
    Mockito.`when`(nomisService.getOffendersAtPrison(PRISON)).thenReturn(Arrays.asList("offender1"))
    prisonService.addPrison(PRISON)
    Mockito.verify(repository).save(
      ArgumentMatchers.any(
        PrisonSupported::class.java,
      ),
    )
  }

  @Test
  fun addPrisonWhichDoesNotExist() {
    Mockito.`when`(repository.existsById(PRISON)).thenReturn(false)
    Mockito.`when`(nomisService.getOffendersAtPrison(PRISON)).thenReturn(emptyList())
    try {
      prisonService.addPrison(PRISON)
      Assertions.fail<Any>("Exception should have been thrown")
    } catch (e: PrisonException) {
      Assertions.assertThat(e.message).isEqualTo("Prison [TEST] is invalid.")
    }
    Mockito.verify(repository, Mockito.never()).save(ArgumentMatchers.any())
  }

  @Test
  fun addPrisonWhichIsAlreadyPresent() {
    Mockito.`when`(repository.existsById(PRISON)).thenReturn(true)
    try {
      prisonService.addPrison(PRISON)
      Assertions.fail<Any>("Exception should have been thrown")
    } catch (e: PrisonException) {
      Assertions.assertThat(e.message).isEqualTo("Prison [TEST] is already present.")
    }
    Mockito.verify(repository, Mockito.never()).save(ArgumentMatchers.any())
  }

  companion object {
    private const val PRISON = "TEST"
  }
}
