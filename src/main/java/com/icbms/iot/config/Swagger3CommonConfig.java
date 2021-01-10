package com.icbms.iot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @Author: Cherry
 * @Date: 2021/1/10
 * @Desc: Swagger3CommonConfig
 */
@Configuration
public class Swagger3CommonConfig {

    /**
     * api接口包扫描路径
     */
    public static final String SWAGGER_SCAN_BASE_PACKAGE = "com.icbms.iot";

    public static final String VERSION = "1.0.0";

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(SWAGGER_SCAN_BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //设置文档的标题
                .title("icbms-iot-svc")
                // 设置文档的描述
                .description("icbms-iot-svc")
                .contact(new Contact("dev", "#", "icbms-iot-svc"))
                .version(VERSION)
                .build();
    }
}
