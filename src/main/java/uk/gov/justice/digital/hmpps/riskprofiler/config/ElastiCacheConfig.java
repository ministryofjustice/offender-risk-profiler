package uk.gov.justice.digital.hmpps.riskprofiler.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.aws.cache.config.annotation.CacheClusterConfig;
import org.springframework.cloud.aws.cache.config.annotation.EnableElastiCache;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "cache.type", havingValue = "elasticache")
@EnableCaching
@EnableElastiCache(value =
        {
                @CacheClusterConfig(name = "escapeAlert"),
                @CacheClusterConfig(name = "socAlert"),
                @CacheClusterConfig(name = "incident")
        },
        defaultExpiration = 30 * 86400)
public class ElastiCacheConfig {
}
