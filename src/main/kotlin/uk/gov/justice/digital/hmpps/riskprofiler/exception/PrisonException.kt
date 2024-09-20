package uk.gov.justice.digital.hmpps.riskprofiler.exception

import java.util.function.Supplier

class PrisonException(message: String?) : RuntimeException(message), Supplier<PrisonException> {
  override fun get(): PrisonException {
    return PrisonException(message)
  }

  @Synchronized
  override fun fillInStackTrace(): Throwable {
    return this
  }

  companion object {
    private const val INVALID_PRISON = "Prison [%s] is invalid."
    private const val PRISON_EXISTS = "Prison [%s] is already present."

    @JvmStatic
    fun exists(id: String?): PrisonException {
      return PrisonException(String.format(PRISON_EXISTS, id))
    }

    @JvmStatic
    fun withId(id: String?): PrisonException {
      return PrisonException(String.format(INVALID_PRISON, id))
    }

    fun withMessage(message: String?): PrisonException {
      return PrisonException(message)
    }

    fun withMessage(message: String?, vararg args: Any?): PrisonException {
      return PrisonException(String.format(message!!, *args))
    }
  }
}
