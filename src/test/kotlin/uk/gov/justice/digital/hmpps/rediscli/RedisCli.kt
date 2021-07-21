package uk.gov.justice.digital.hmpps.rediscli

import org.apache.commons.lang3.ArrayUtils
import redis.clients.jedis.Jedis
import java.net.URI
import java.net.URISyntaxException

object RedisCli {
  @Throws(URISyntaxException::class)
  @JvmStatic
  fun main(args: Array<String>) {
    Jedis(URI("rediss://localhost:6379")).use { jedis ->
      jedis.auth("xxxx") // use auth_token from the redis secret
      jedis["foo"] = "bar"
      val value = jedis["foo"]
      println(value)
      println(ArrayUtils.toString(jedis.dump("incident::G8220GL")))
      jedis.flushDB()
      // println(jedis.info())
    }
  }
}
