package com.cetcxl.xlpay.payuser.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.cetcxl.xlpay.payuser.dao"})
public class MybatisPlusExtraConfig {

}

