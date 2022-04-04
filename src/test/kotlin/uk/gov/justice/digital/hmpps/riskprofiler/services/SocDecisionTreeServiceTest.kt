package uk.gov.justice.digital.hmpps.riskprofiler.services

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepositoryFactory
import uk.gov.justice.digital.hmpps.riskprofiler.dao.OcgRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.OcgmRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PrasRepository
import uk.gov.justice.digital.hmpps.riskprofiler.dao.ViperRepository
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocg
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Ocgm
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.OcgmList
import uk.gov.justice.digital.hmpps.riskprofiler.datasourcemodel.Pras
import uk.gov.justice.digital.hmpps.riskprofiler.model.Alert
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class SocDecisionTreeServiceTest {
  private lateinit var service: SocDecisionTreeService

  private val nomisService: NomisService = mock()
  private val prasRepo: PrasRepository = mock<PrasRepository>()
  private val ocgRepo: OcgRepository = mock()
  private val ocgmRepo: OcgmRepository = mock()
  private val viperRepo: ViperRepository = mock()

  @BeforeEach
  fun setup() {
    val factory = DataRepositoryFactory(ocgmRepo, ocgRepo, prasRepo, viperRepo)
    service = SocDecisionTreeService(factory, nomisService)
  }

  @Test
  fun testOnPrasFile() {
    Mockito.`when`(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(Pras(OFFENDER_1))
    )
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isTrue()
  }

  @Test
  fun testNotOnPrasFileAndBandNotInList() {
    val xfo = Alert(true, false, "XFO")
    xfo.dateCreated = LocalDate.now().minusMonths(11)
    val xd = Alert(false, false, "XD")
    xd.dateExpires = LocalDate.now().minusYears(2)
    Mockito.`when`(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())

    Mockito.`when`(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(OcgmList(OFFENDER_1, Ocgm(OFFENDER_1, "123", null), null))
    )

    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("123")))
      .thenReturn(Optional.of(Ocg("id", "5c")))
    Mockito.`when`<List<Alert>>(nomisService.getSocListAlertsForOffender(OFFENDER_1))
      .thenReturn(java.util.List.of(xfo, xd))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isFalse()
  }

  @Test
  fun testNotOnPrasFileAndBandInList() {
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(OcgmList(OFFENDER_1, Ocgm(OFFENDER_1, "1234", null), null))
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("1234")))
      .thenReturn(Optional.of(Ocg("id", "2a")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isFalse()
  }

  @Test
  fun testNotOnPrasFileAndBandInListAndPrincipleStanding() {
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(
        OcgmList(
          OFFENDER_1,
          Ocgm(OFFENDER_1, "12345", SocDecisionTreeService.PRINCIPAL_SUBJECT),
          null
        )
      )
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("12345")))
      .thenReturn(Optional.of(Ocg("id", "2a")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isTrue()
  }

  @Test
  fun testNotOnPrasFileAndBandInListAndMultiplePrincipleStanding() {
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(
        OcgmList(
          OFFENDER_1,
          Ocgm(OFFENDER_1, "12345", "${SocDecisionTreeService.PRINCIPAL_SUBJECT},${SocDecisionTreeService.PRINCIPAL_SUBJECT},${SocDecisionTreeService.PRINCIPAL_SUBJECT}"),
          null
        )
      )
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("12345")))
      .thenReturn(Optional.of(Ocg("id", "2a")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isTrue()
  }

  @Test
  fun testNotOnPrasFileAndBandInListAndNotPrincipleStanding() {
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(
        OcgmList(
          OFFENDER_1,
          Ocgm(OFFENDER_1, "12345", null),
          null
        )
      )
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("12345")))
      .thenReturn(Optional.of(Ocg("id", "2a")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isFalse()
  }

  @Test
  fun testNotOnPrasFileAndNotInBandInListAndPrincipleStanding() {
    Mockito.`when`(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(
        OcgmList(
          OFFENDER_1,
          Ocgm(OFFENDER_1, "123456", SocDecisionTreeService.PRINCIPAL_SUBJECT),
          null
        )
      )
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("123456")))
      .thenReturn(Optional.of(Ocg("id", "4a")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isTrue()
  }

  @Test
  fun testNotOnPrasFileAndBandNotInListWithOldAlerts() {
    val now = LocalDate.now()
    val xfo = Alert(true, false, "XFO")
    xfo.dateCreated = now.minusMonths(13)
    val xd = Alert(false, false, "XD")
    xd.dateExpires = now.minusMonths(16)
    xd.expired = true
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(
        OcgmList(
          OFFENDER_1,
          Ocgm(OFFENDER_1, "123", null),
          null
        )
      )
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("123")))
      .thenReturn(Optional.of(Ocg("id", "5c")))
    Mockito.`when`<List<Alert>>(nomisService.getSocListAlertsForOffender(OFFENDER_1))
      .thenReturn(java.util.List.of(xfo, xd))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isFalse()
  }

  @Test
  fun testNotOnPrasFileAndNoOcgmWithActiveAlerts() {
    val now = LocalDate.now()
    val xfo = Alert(true, false, "XFO")
    xfo.dateCreated = now.minusMonths(11)
    val xd = Alert(false, false, "XD")
    xd.dateExpires = now.minusYears(2)
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.empty()
    )
    Mockito.`when`<List<Alert>>(nomisService.getSocListAlertsForOffender(OFFENDER_1))
      .thenReturn(java.util.List.of(xfo, xd))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isFalse()
  }

  @Test
  fun testNotOnPrasFileAndNoOcgmWithOldAlerts() {
    val now = LocalDate.now()
    val xfo = Alert(true, false, "XFO")
    xfo.dateCreated = now.minusMonths(13)
    val xd = Alert(false, false, "XD")
    xd.dateExpires = now.minusMonths(16)
    xd.expired = true
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.empty()
    )
    Mockito.`when`<List<Alert>>(nomisService.getSocListAlertsForOffender(OFFENDER_1))
      .thenReturn(java.util.List.of(xfo, xd))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isFalse()
  }

  @Test
  fun testNotOnPrasFileAndHasOcgmNotNoOcgWithOldAlerts() {
    val now = LocalDate.now()
    val xfo = Alert(true, false, "XFO")
    xfo.dateCreated = now.minusMonths(13)
    val xd = Alert(false, false, "XD")
    xd.dateExpires = now.minusMonths(16)
    xd.expired = true
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(
        OcgmList(
          OFFENDER_1,
          Ocgm(OFFENDER_1, "123", null),
          null
        )
      )
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("123"))).thenReturn(Optional.empty())
    Mockito.`when`<List<Alert>>(nomisService.getSocListAlertsForOffender(OFFENDER_1))
      .thenReturn(java.util.List.of(xfo, xd))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isFalse()
  }

  @Test
  fun testNotOnPrasFileAndOneWithInBandInListAndAnotherEntryWithoutAndPrincipleStanding() {
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    val ocgm1 = Ocgm(OFFENDER_1, "123456", "SomethingElse")
    val ocgm2 = Ocgm(OFFENDER_1, "1234567", "SomethingElse")
    val ocgmList = OcgmList(OFFENDER_1, null, java.util.List.of(ocgm1, ocgm2))
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(ocgmList)
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("123456")))
      .thenReturn(Optional.of(Ocg("id", "4a")))
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("1234567")))
      .thenReturn(Optional.of(Ocg("id", "1a")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isFalse()
  }

  @Test
  fun testNotOnPrasFileAndMultipleOffendersForSameNomsIDOneWithBand1a() {
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    val ocgm1 = Ocgm(OFFENDER_1, "123456", "SomethingElse")
    val ocgm2 = Ocgm(OFFENDER_1, "1234567", "SomethingElse")
    val ocgm3 = Ocgm(OFFENDER_1, "1234568", SocDecisionTreeService.PRINCIPAL_SUBJECT)
    val ocgmList = OcgmList(OFFENDER_1, null, java.util.List.of(ocgm1, ocgm2, ocgm3))
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(ocgmList)
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("123456")))
      .thenReturn(Optional.of(Ocg("id", "4a")))
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("1234567")))
      .thenReturn(Optional.of(Ocg("id", "5a")))
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("1234568")))
      .thenReturn(Optional.of(Ocg("id", "1a")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isTrue()
  }

  @Test
  fun testNotOnPrasFileAndMultipleOffendersForSameNomsIDOneWithBand5aButPrincipal() {
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    val ocgm1 = Ocgm(OFFENDER_1, "123456", "SomethingElse")
    val ocgm2 = Ocgm(OFFENDER_1, "1234567", SocDecisionTreeService.PRINCIPAL_SUBJECT)
    val ocgm3 = Ocgm(OFFENDER_1, "1234568", "SomethingElse")
    val ocgmList = OcgmList(OFFENDER_1, null, java.util.List.of(ocgm1, ocgm2, ocgm3))
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(ocgmList)
    )
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("123456")))
      .thenReturn(Optional.of(Ocg("id", "4a")))
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("1234567")))
      .thenReturn(Optional.of(Ocg("id", "5a")))
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("1234568")))
      .thenReturn(Optional.of(Ocg("id", "1a")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isTrue()
  }

  @Test
  fun testPrisonerInListButBandMissing() {
    Mockito.`when`<Optional<Pras>>(prasRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1)))
      .thenReturn(Optional.empty())
    Mockito.`when`<Optional<OcgmList>>(ocgmRepo.getByKey(ArgumentMatchers.eq<String>(OFFENDER_1))).thenReturn(
      Optional.of(
        OcgmList(
          OFFENDER_1,
          Ocgm(OFFENDER_1, "12345", SocDecisionTreeService.PRINCIPAL_SUBJECT),
          null
        )
      )
    )

    // return ocg with no band:
    Mockito.`when`<Optional<Ocg>>(ocgRepo.getByKey(ArgumentMatchers.eq<String>("12345")))
      .thenReturn(Optional.of(Ocg("id")))
    val socProfile: SocProfile = service.getSocData(OFFENDER_1)
    Assertions.assertThat(socProfile.provisionalCategorisation).isEqualTo("C")
    Assertions.assertThat(socProfile.transferToSecurity).isTrue()
  }

  companion object {
    private const val OFFENDER_1 = "AB1234A"
  }
}
