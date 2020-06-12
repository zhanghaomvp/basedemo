package com.cetcxl.usercenter.server.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.cetcxl.usercenter.server.dao")
public class MybatisPlusConfig {
}
