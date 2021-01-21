package com.icbms.iot.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, RealTimeMessage> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, RealTimeMessage> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
}
