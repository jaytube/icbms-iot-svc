package com.icbms.iot;

import com.icbms.iot.inbound.service.IotRoundRobinController;
import com.icbms.iot.inbound.service.MqttMsgWorker;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan("com.icbms.iot.mapper")
@SpringBootApplication
@EnableAsync
public class IcbmsIotSvcApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(IcbmsIotSvcApplication.class, args);
		context.getBean(IotRoundRobinController.class).roundRobinControl();
		context.getBean(MqttMsgWorker.class).processMsg();
	}

}
