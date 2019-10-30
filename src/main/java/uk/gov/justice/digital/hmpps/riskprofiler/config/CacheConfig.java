package uk.gov.justice.digital.hmpps.riskprofiler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.redis.cache.timeout-days}")
    private int timeoutDays;
    @Value("${spring.redis.host}")
    private String server;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.ssl}")
    private boolean ssl;
    @Value("${spring.redis.client-name}")
    private String clientName;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        final RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(server, port);
        redisConfig.setPassword(password);

        final JedisClientConfigurationBuilder jedisClientConfigurationBuilder = JedisClientConfiguration.builder();
        if (ssl) {
            jedisClientConfigurationBuilder.useSsl();
        }
        jedisClientConfigurationBuilder.usePooling();
        jedisClientConfigurationBuilder.clientName(clientName);

        return new JedisConnectionFactory(redisConfig, jedisClientConfigurationBuilder.build());
    }

    @Bean
    public RedisCacheManager cacheManager() {
        final RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(timeoutDays));

        return RedisCacheManager.builder(jedisConnectionFactory())
                .cacheDefaults(cacheConfig)
                .build();
    }
}
