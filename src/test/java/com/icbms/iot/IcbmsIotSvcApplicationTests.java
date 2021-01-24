package com.icbms.iot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class IcbmsIotSvcApplicationTests {

	@Autowired
	StringRedisTemplate redisTemplate;
	@Test
	void contextLoads() {
		//System.out.println(redisTemplate);
		redisTemplate.opsForHash().put("AAA", "test", "test");
	}

}
