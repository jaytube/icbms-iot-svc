package com.icbms.iot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.icbms.iot.mapper")
@SpringBootApplication
@EnableRetry
@EnableAsync
@EnableScheduling
public class IcbmsIotSvcApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(IcbmsIotSvcApplication.class, args);
		//context.getBean(IotRoundRobinController.class).roundRobinControl();
		//context.getBean(MqttMsgWorker.class).processMsg();
	}

}
