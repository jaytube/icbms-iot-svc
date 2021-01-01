package com.icbms.iot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.icbms.iot.mapper")
@SpringBootApplication
public class IcbmsIotSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(IcbmsIotSvcApplication.class, args);
	}

}
