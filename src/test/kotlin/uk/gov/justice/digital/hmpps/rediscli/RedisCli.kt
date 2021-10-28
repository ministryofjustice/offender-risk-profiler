package uk.gov.justice.digital.hmpps.rediscli

import redis.clients.jedis.Jedis
import java.net.URI
import java.net.URISyntaxException

/**
 * Client to inquire or manipulate the redis cache database
 */
object RedisCli {
  @Throws(URISyntaxException::class)
  @JvmStatic
  fun main(args: Array<String>) {
    Jedis(URI("rediss://localhost:6379")).use { jedis ->
      jedis.auth("0818xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx9b7379")
      // use auth_token from the redis secret,
      // then to forward to Redis server:
      // kc -n offender-categorisation-prod run port-forward-pod --generator=run-pod/v1 --image=ministryofjustice/port-forward --port=6379 --env="REMOTE_HOST=master.xxxxxxxxxxxxxxxxxxxxx.cache.amazonaws.com" --env="LOCAL_PORT=6379" --env="REMOTE_PORT=6379"
      // kc -n offender-categorisation-prod port-forward port-forward-pod 6379:6379

      // jedis["foo"] = "bar"
      // jedis.del("incident::A9362AP")
      // DO NOT USE on large cache, locks redis !
      // jedis.keys("[ia]*").forEach{s -> println(s)}
      // println(jedis["incident::G1234AB"])
      // println(jedis.dump("incident::G1234AB"))
      // jedis.del("akey")
      // jedis.flushDB() // delete all entries
      println(jedis.info())
    }
  }
}
