package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {
  @Value("\${spring.redis.cache.timeout-days}")
  private val timeoutDays = 0

  @Value("\${spring.redis.host}")
  private val server: String? = null

  @Value("\${spring.redis.port}")
  private val port = 0

  @Value("\${spring.redis.password}")
  private val password: String? = null

  @Value("\${spring.redis.ssl}")
  private val ssl = false

  @Value("\${spring.redis.client-name}")
  private val clientName: String? = null

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

  @Bean
  fun cacheManager(): RedisCacheManager {
    val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
      .entryTtl(Duration.ofDays(timeoutDays.toLong()))
    return RedisCacheManager.builder(jedisConnectionFactory())
      .cacheDefaults(cacheConfig)
      .build()
  }
}
