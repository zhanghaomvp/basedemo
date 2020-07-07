package com.cetcxl.xlpay.admin.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.cetcxl.xlpay.admin.dao"})
public class MybatisPlusExtraConfig {

}

