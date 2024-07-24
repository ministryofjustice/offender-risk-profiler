package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

// @Configuration
class CacheConfig {
 //  @Bean
  fun redisConnectionFactory(): LettuceConnectionFactory {
    return LettuceConnectionFactory()
  }

  // @Bean
  fun redisTemplate(): RedisTemplate<*, *> {
    val template = RedisTemplate<ByteArray, ByteArray>()
    template.connectionFactory = redisConnectionFactory()
    return template
  }
}
