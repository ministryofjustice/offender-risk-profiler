package uk.gov.justice.digital.hmpps.riskprofiler.model

data class ProfileMessagePayload(
  val escape: EscapeProfile,
  val soc: SocProfile,
  val violence: ViolenceProfile,
)
