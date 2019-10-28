package uk.gov.justice.digital.hmpps.riskprofiler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "cache.type", havingValue = "redis")
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

    @Bean
    public RedisCacheManager cacheManager() {

        final RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(server, port);
        redisConfig.setPassword(password);

        final RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(timeoutDays));

        return RedisCacheManager.builder(new JedisConnectionFactory(redisConfig))
                .cacheDefaults(cacheConfig)
                .build();
    }
}
