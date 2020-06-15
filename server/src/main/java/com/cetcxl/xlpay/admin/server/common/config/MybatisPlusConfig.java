package com.cetcxl.xlpay.admin.server.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.cetcxl.xlpay.admin.server.dao")
public class MybatisPlusConfig {
}
