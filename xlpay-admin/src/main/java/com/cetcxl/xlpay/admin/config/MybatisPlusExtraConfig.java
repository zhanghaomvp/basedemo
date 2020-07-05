package com.cetcxl.xlpay.admin.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.cetcxl.xlpay.admin.dao", "com.cetcxl.xlpay.common.dao"})
public class MybatisPlusExtraConfig {

}

