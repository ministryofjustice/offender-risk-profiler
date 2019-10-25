package uk.gov.justice.digital.hmpps.riskprofiler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${redis.cache.timeoutDays:30}")
    private int timeoutDays;
    @Value("${redis.host:localhost}")
    private String server;
    @Value("${redis.port:6379}")
    private int port;
    @Value("${redis.auth.token:}")
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
