package uk.gov.justice.digital.hmpps.riskprofiler.model

data class ProfileMessagePayload(
  private val escape: EscapeProfile? = null,
  // private val extremism: ExtremismProfile? = null,
  private val soc: SocProfile? = null,
  private val violence: ViolenceProfile? = null,
)
