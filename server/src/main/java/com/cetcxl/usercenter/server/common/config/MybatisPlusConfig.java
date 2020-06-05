package com.cetcxl.usercenter.server.common.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.cetcxl.usercenter.server")
public class MybatisPlusConfig {
}
