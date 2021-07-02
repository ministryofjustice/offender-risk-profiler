package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderBooking
import uk.gov.justice.digital.hmpps.riskprofiler.model.OffenderSentenceTerms

@ExtendWith(MockitoExtension::class)
class LifeDecisionTreeServiceTest {
  private lateinit var service: LifeDecisionTreeService
  private val lifeSentence = OffenderSentenceTerms(BOOKING_1, true)
  private val nonLifeSentence = OffenderSentenceTerms(BOOKING_1, false)
  private val lifeCode = OffenderBooking(BOOKING_1, null, null, "CFLIFE")
  private val nonLifeCode = OffenderBooking(BOOKING_1, null, null, "OTHER")

  @Mock
  private val nomisService: NomisService? = null
  @BeforeEach
  fun setup() {
    service = LifeDecisionTreeService(nomisService!!)
  }

  @Test
  fun testWhenLifeFlagTrue() {
    Mockito.`when`(nomisService!!.getBooking(OFFENDER_1)).thenReturn(BOOKING_1)
    Mockito.`when`(nomisService.getSentencesForOffender(BOOKING_1)).thenReturn(listOf(nonLifeSentence, lifeSentence))
    val (nomsId, provisionalCategorisation, life) = service.getLifeProfile(OFFENDER_1)
    Assertions.assertThat(provisionalCategorisation).isEqualTo("B")
    Assertions.assertThat(nomsId).isEqualTo(OFFENDER_1)
    Assertions.assertThat(life).isTrue
  }

  @Test
  fun testWhenLifeCode() {
    Mockito.`when`(nomisService!!.getBooking(OFFENDER_1)).thenReturn(BOOKING_1)
    Mockito.`when`(nomisService.getSentencesForOffender(BOOKING_1)).thenReturn(listOf(nonLifeSentence))
    Mockito.`when`(nomisService.getBookingDetails(BOOKING_1)).thenReturn(listOf(nonLifeCode, lifeCode))
    val (nomsId, provisionalCategorisation, life) = service.getLifeProfile(OFFENDER_1)
    Assertions.assertThat(provisionalCategorisation).isEqualTo("B")
    Assertions.assertThat(nomsId).isEqualTo(OFFENDER_1)
    Assertions.assertThat(life).isTrue
  }

  @Test
  fun testWhenMurder() {
    Mockito.`when`(nomisService!!.getBooking(OFFENDER_1)).thenReturn(BOOKING_1)
    Mockito.`when`(nomisService.getSentencesForOffender(BOOKING_1)).thenReturn(listOf(nonLifeSentence))
    Mockito.`when`(nomisService.getBookingDetails(BOOKING_1)).thenReturn(listOf(nonLifeCode))
    Mockito.`when`(nomisService.getMainOffences(BOOKING_1)).thenReturn(listOf("Murder etc."))
    val (nomsId, provisionalCategorisation, life) = service.getLifeProfile(OFFENDER_1)
    Assertions.assertThat(provisionalCategorisation).isEqualTo("B")
    Assertions.assertThat(nomsId).isEqualTo(OFFENDER_1)
    Assertions.assertThat(life).isTrue
  }

  @Test
  fun testWhenNotLife() {
    Mockito.`when`(nomisService!!.getBooking(OFFENDER_1)).thenReturn(BOOKING_1)
    Mockito.`when`(nomisService.getSentencesForOffender(BOOKING_1)).thenReturn(listOf(nonLifeSentence))
    Mockito.`when`(nomisService.getBookingDetails(BOOKING_1)).thenReturn(listOf(nonLifeCode, nonLifeCode))
    Mockito.`when`(nomisService.getMainOffences(BOOKING_1)).thenReturn(listOf("Trivial etc.", "another"))
    val (nomsId, provisionalCategorisation, life) = service.getLifeProfile(OFFENDER_1)
    Assertions.assertThat(provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(nomsId).isEqualTo(OFFENDER_1)
    Assertions.assertThat(life).isFalse
  }

  companion object {
    private const val OFFENDER_1 = "A1234AB"
    private const val BOOKING_1 = -1L
  }
}
