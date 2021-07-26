package uk.gov.justice.digital.hmpps.rediscli

import redis.clients.jedis.Jedis
import java.net.URI
import java.net.URISyntaxException

object RedisCli {
  @Throws(URISyntaxException::class)
  @JvmStatic
  fun main(args: Array<String>) {
    Jedis(URI("rediss://localhost:6379")).use { jedis ->
      jedis.auth("xxxx") // use auth_token from the redis secret
      // jedis["foo"] = "bar"
      println(jedis["incident::G1234AB"])
      println(jedis.dump("incident::G1234AB"))
      // jedis.flushDB()
      println(jedis.info())
    }
  }
}
