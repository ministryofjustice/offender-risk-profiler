package uk.gov.justice.digital.hmpps.riskprofiler.services

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.riskprofiler.dao.PreviousProfileRepository
import uk.gov.justice.digital.hmpps.riskprofiler.model.EscapeProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.PreviousProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.ProfileMessagePayload
import uk.gov.justice.digital.hmpps.riskprofiler.model.RiskProfileChange
import uk.gov.justice.digital.hmpps.riskprofiler.model.SocProfile
import uk.gov.justice.digital.hmpps.riskprofiler.model.ViolenceProfile
import java.io.IOException
import java.time.LocalDateTime

@Service
class PollPrisonersService(
  private val socDecisionTreeService: SocDecisionTreeService,
  private val violenceDecisionTreeService: ViolenceDecisionTreeService,
  private val escapeDecisionTreeService: EscapeDecisionTreeService,
  private val previousProfileRepository: PreviousProfileRepository,
  private val telemetryClient: TelemetryClient,
  private val sqsService: SQSService
) {
  private val jacksonMapper = ObjectMapper()
  // private val defaultFromDateTime = LocalDateTime.of(2019, 10, 25, 0, 0)

  @Transactional
  fun pollPrisoner(offenderNo: String) {
    try {
      val socObject = socDecisionTreeService.getSocData(offenderNo)
      val violenceObject = violenceDecisionTreeService.getViolenceProfile(offenderNo)
      val escapeObject = escapeDecisionTreeService.getEscapeProfile(offenderNo)
      // Life Decision Tree deliberately omitted
      // Extremism deliberately omitted. offender is referred to security only when a categorisation is started
      val soc = jacksonMapper.writeValueAsString(socObject)
      val violence = jacksonMapper.writeValueAsString(violenceObject)
      val escape = jacksonMapper.writeValueAsString(escapeObject)

      // Check if in db
      val previousProfile = previousProfileRepository.findById(offenderNo)
      previousProfile.ifPresentOrElse(
        { existing: PreviousProfile? ->
          // Compare with existing stored values
          if (!(existing!!.soc == soc && existing.violence == violence && existing.escape == escape)) {
            // Update db with new data:
            log.info("Change detected for {}", offenderNo)
            buildAndSendRiskProfilePayload(offenderNo, socObject, violenceObject, escapeObject, existing)
            existing.soc = soc
            existing.violence = violence
            existing.escape = escape
            existing.executeDateTime = LocalDateTime.now()
          }
        }
      ) {

        // if not there, just add
        previousProfileRepository.save(
          PreviousProfile(
            offenderNo,
            escape, soc, violence, LocalDateTime.now()
          )
        )
        log.info("Added new offender {} to DB", offenderNo)
      }
    } catch (e: Exception) {
      raiseProcessingError(offenderNo, e)
    }
  }

  private fun buildAndSendRiskProfilePayload(
    offenderNo: String,
    socObject: SocProfile,
    violenceObject: ViolenceProfile,
    escapeObject: EscapeProfile,
    existing: PreviousProfile?
  ) {
    val newProfile = ProfileMessagePayload(escapeObject, socObject, violenceObject)
    val oldProfile: ProfileMessagePayload
    try {
      oldProfile = ProfileMessagePayload(
        jacksonMapper.readValue(existing!!.escape, EscapeProfile::class.java),
        jacksonMapper.readValue(existing.soc, SocProfile::class.java),
        jacksonMapper.readValue(existing.violence, ViolenceProfile::class.java)
      )
      val payload = RiskProfileChange(oldProfile, newProfile, offenderNo, existing.executeDateTime)
      log.info("Reporting risk change to queue for offender {}", offenderNo)
      sqsService.sendRiskProfileChangeMessage(payload)
    } catch (e: IOException) {
      log.error("Problem creating risk profile change message for $offenderNo", e)
    }
  }

  private fun raiseProcessingError(offenderNo: String, e: Exception) {
    log.error("pollPrisoner: Exception thrown for $offenderNo", e)
    val logMap = HashMap<String, String>()
    logMap["offenderNo"] = offenderNo
    telemetryClient.trackException(e, logMap, null)
  }

  companion object {
    private val log = LoggerFactory.getLogger(PollPrisonersService::class.java)
  }

  init {
    jacksonMapper.registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }
}
