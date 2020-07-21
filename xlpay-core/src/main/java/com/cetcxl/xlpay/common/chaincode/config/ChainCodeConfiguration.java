package com.cetcxl.xlpay.common.chaincode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName ChainCode
 * @Description 智能合约参数
 * @Author liuanyicun
 * @Date 2019/3/7 14:53
 * @Version 1.0
 **/
@Component
@Data
@ConfigurationProperties(prefix = "chain-code")
public class ChainCodeConfiguration {
    private Boolean chainCodeSwitch;
    private String readAddress;
    private String chainCodeIp;
    private String chainCodeName;
    private String appKey;
    private String appSecret;
}
