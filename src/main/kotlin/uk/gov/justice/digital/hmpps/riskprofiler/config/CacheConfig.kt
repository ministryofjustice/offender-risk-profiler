package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import uk.gov.justice.digital.hmpps.riskprofiler.model.PrisonSupported
import java.awt.print.Book
import java.time.Duration


class CacheConfig {
  @Value("\${spring.redis.cache.timeout-days}")
  private val timeoutDays = 0

  @Value("\${spring.data.redis.host}")
  private val server: String? = null

  @Value("\${spring.data.redis.port}")
  private val port = 0

  @Value("\${spring.data.redis.password}")
  private val password: String? = null

  @Value("\${spring.data.redis.ssl.enabled}")
  private val ssl = false

  @Value("\${spring.data.redis.client-name}")
  private val clientName: String? = null

  /*
  @Bean
  fun jedisConnectionFactory(): JedisConnectionFactory {
    val redisConfig = RedisStandaloneConfiguration(server!!, port)
    redisConfig.setPassword(password)
    val jedisClientConfigurationBuilder = JedisClientConfiguration.builder()
    if (ssl) {
      jedisClientConfigurationBuilder.useSsl()
    }
    jedisClientConfigurationBuilder.usePooling()
    jedisClientConfigurationBuilder.clientName(clientName!!)
    return JedisConnectionFactory(redisConfig, jedisClientConfigurationBuilder.build())
  }

*/
/*
  @Bean
  fun redisConnectionFactory() : RedisConnectionFactory {
    val redisConnectionFactory = RedisConnectionFactory()
  }
*/
  @Bean
  fun redisTemplate(connectionFactory: RedisConnectionFactory?): RedisTemplate<Long, PrisonSupported> {

    val template = RedisTemplate<Long, PrisonSupported>()
    template.connectionFactory = connectionFactory
    // Add some specific configuration here. Key serializers, etc.
    return template
  }
}
