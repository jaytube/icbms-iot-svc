package com.icbms.iot.config;

import com.icbms.iot.dto.RealTimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

    /*@Bean
    public RedisTemplate<String, RealTimeMessage> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, RealTimeMessage> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }*/
}
