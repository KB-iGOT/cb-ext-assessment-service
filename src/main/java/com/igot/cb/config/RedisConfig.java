package com.igot.cb.config;

import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Slf4j
public class RedisConfig {
    @Autowired
    CbExtAssessmentServerProperties cbExtAssessmentServerProperties;

    @Bean
    public JedisPool jedisPool() {
        return buildPool(cbExtAssessmentServerProperties.getRedisHostName(),
                Integer.parseInt(cbExtAssessmentServerProperties.getRedisPort()),
                cbExtAssessmentServerProperties.isRedisPasswordRequired(),
                cbExtAssessmentServerProperties.getRedisUsername(),
                cbExtAssessmentServerProperties.getRedisPassword());
    }

    @Bean
    public JedisPool jedisDataPopulationPool() {
        return buildPool(cbExtAssessmentServerProperties.getRedisDataHostName(),
                Integer.parseInt(cbExtAssessmentServerProperties.getRedisDataPort()),
                cbExtAssessmentServerProperties.isRedisDataPasswordRequired(),
                cbExtAssessmentServerProperties.getRedisDataUsername(),
                cbExtAssessmentServerProperties.getRedisDataPassword());
    }

    /**
     * Builds one pool, authenticated or not, for whichever of the two Redis servers the arguments
     * describe. The two servers are configured independently, so one may require credentials while
     * the other does not.
     */
    private JedisPool buildPool(String host, int port, boolean passwordRequired, String username, String password) {
        final JedisPoolConfig poolConfig = buildPoolConfig();

        if (!passwordRequired) {
            log.warn("Redis pool for {}:{} created WITHOUT authentication - if that server has requirepass set, every operation will fail with NOAUTH and be swallowed as a cache miss", host, port);
            return new JedisPool(poolConfig, host, port);
        }
        if (StringUtils.isBlank(username)) {
            throw new IllegalStateException("A username is required for the Redis instance at " + host + ":" + port + " but not configured");
        }
        if (StringUtils.isBlank(password)) {
            throw new IllegalStateException("A password is required for the Redis instance at " + host + ":" + port + " but not configured");
        }
        // The username is safe to log and is what makes an ACL misconfiguration diagnosable from the
        // startup line alone; the password must never appear here.
        log.info("Redis pool for {}:{} created with authentication enabled for user '{}'", host, port, username);
        return new JedisPool(poolConfig, host, port, username, password);
    }

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(cbExtAssessmentServerProperties.getMaxIdle());
        poolConfig.setMaxTotal(cbExtAssessmentServerProperties.getMaxActive());
        poolConfig.setMinIdle(cbExtAssessmentServerProperties.getMinIdle());
        poolConfig.setTestOnBorrow(cbExtAssessmentServerProperties.isTestOnBorrow());
        poolConfig.setTestOnReturn(cbExtAssessmentServerProperties.isTestOnReturn());
        poolConfig.setTestWhileIdle(cbExtAssessmentServerProperties.isTestWhileIdle());
        poolConfig.setMinEvictableIdleTimeMillis(cbExtAssessmentServerProperties.getMinEvictableIdleTime());
        poolConfig.setTimeBetweenEvictionRunsMillis(cbExtAssessmentServerProperties.getTimeBetweenEvictionRuns());
        poolConfig.setNumTestsPerEvictionRun(cbExtAssessmentServerProperties.getNumTestsPerEvictionRun());
        poolConfig.setBlockWhenExhausted(cbExtAssessmentServerProperties.isBlockWhenExhausted());
        return poolConfig;
    }
}