package uk.gov.justice.digital.hmpps.riskprofiler.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.GenericToStringSerializer


@Configuration
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

  @Bean
  fun jedisConnectionFactory(): JedisConnectionFactory {
    return JedisConnectionFactory()
  }

  @Bean
  fun redisTemplate(): RedisTemplate<String, Any> {
    val template = RedisTemplate<String, Any>()
    template.connectionFactory = jedisConnectionFactory()
    template.valueSerializer = GenericToStringSerializer(Any::class.java)
    return template
  }

  @Bean
  fun messageListener(): MessageListenerAdapter {
    return MessageListenerAdapter(RedisMessageSubscriber())
  }

  @Bean
  fun redisContainer(): RedisMessageListenerContainer {
    val container = RedisMessageListenerContainer()
    container.connectionFactory = jedisConnectionFactory()
    container.addMessageListener(messageListener(), topic())
    return container
  }

  @Bean
  fun redisPublisher(): MessagePublisher {
    return RedisMessagePublisher(redisTemplate(), topic())
  }

  @Bean
  fun topic(): ChannelTopic {
    return ChannelTopic("pubsub:queue")
  }
}
