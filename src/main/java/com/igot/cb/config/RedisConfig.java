package com.igot.cb.config;

import com.igot.cb.common.util.CbExtAssessmentServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {
    @Autowired
    CbExtAssessmentServerProperties cbExtAssessmentServerProperties;

    @Bean
    public JedisPool jedisPool() {
        return new JedisPool(buildPoolConfig(), cbExtAssessmentServerProperties.getRedisHostName(), Integer.parseInt(cbExtAssessmentServerProperties.getRedisPort()));
    }

    @Bean
    public JedisPool jedisDataPopulationPool() {
        final JedisPoolConfig poolConfig = buildPoolConfig();
        return new JedisPool(poolConfig, cbExtAssessmentServerProperties.getRedisDataHostName(),
                Integer.parseInt(cbExtAssessmentServerProperties.getRedisDataPort()));
    }
    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(128);
        poolConfig.setMaxTotal(3000);
        poolConfig.setMinIdle(100);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(120000);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
}