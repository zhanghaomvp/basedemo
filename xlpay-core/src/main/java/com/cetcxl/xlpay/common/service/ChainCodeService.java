package com.cetcxl.xlpay.common.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cetcxl.xlpay.common.chaincode.config.ChainCodeConfiguration;
import com.cetcxl.xlpay.common.chaincode.constants.ChainCodeConstant;
import com.cetcxl.xlpay.common.chaincode.entity.BusinessWallet;
import com.cetcxl.xlpay.common.chaincode.entity.CheckSlip;
import com.cetcxl.xlpay.common.chaincode.entity.Order;
import com.cetcxl.xlpay.common.chaincode.entity.PersonalWallet;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.*;

/**
 * @ClassName ChainCodeService
 * @Description 区块链记链
 * @Author liuanyicun
 * @Date 2019/3/8 16:10
 * @Version 1.0
 **/
@Slf4j
@Component
public class ChainCodeService {

    @Autowired
    private ChainCodeConfiguration configuration;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 交易记录上链
     *
     * @param order          交易单
     * @param personalWallet 个人钱包
     * @param businessWallet 商家钱包
     */
    public void saveDealingRecord(Order order, PersonalWallet personalWallet, BusinessWallet businessWallet) {
        if (!configuration.getChainCodeSwitch()) {
            return;
        }

        order.sign();
        personalWallet.sign();

        JSONObject json = initialRequestJson();
        String[] args;

        if (Objects.isNull(businessWallet)) {
            args = new String[]{JSONObject.toJSONString(order), JSONObject.toJSONString(personalWallet)};
        } else {
            businessWallet.sign();
            args = new String[]{JSONObject.toJSONString(order), JSONObject.toJSONString(personalWallet), JSONObject.toJSONString(businessWallet)};
        }

        json.put("args", args);
        json.put("fcn", ChainCodeConstant.SAVE_DEALING_RECORD_FUNC);

        log.info("交易记录上链请求数据[{}]", json.toString());
        Result result = sendRequest(json);

        if (Objects.isNull(result) || result.getCode() != 0) {
            log.error("saveDealingRecord error :" + result.getMsg());
            throw new BaseRuntimeException(CHAIN_CODE_SAVE_DEALING_RECORD_ERROR);
        }
    }

    /**
     * 结算记录上链
     *
     * @param checkSlip 结算单
     */
    public void saveCheckSlip(CheckSlip checkSlip) {
        if (!configuration.getChainCodeSwitch()) {
            return;
        }

        checkSlip.sign();

        JSONObject json = initialRequestJson();
        json.put("args", new String[]{JSONObject.toJSONString(checkSlip)});
        json.put("fcn", ChainCodeConstant.SAVE_CHECK_SLIP_FUNC);

        log.info("结算记录上链请求数据[{}]", json.toString());
        Result result = sendRequest(json);

        if (Objects.isNull(result) || result.getCode() != 0) {
            log.error("saveCheckSlip error :" + result.getMsg());
            throw new BaseRuntimeException(CHAIN_CODE_SAVE_CHECK_SLIP_ERROR);
        }
    }

    private JSONObject initialRequestJson() {
        JSONObject json = new JSONObject();
        json.put("chaincodeName", configuration.getChainCodeName());
        json.put("appKey", configuration.getAppKey());
        json.put("appSecret", configuration.getAppSecret());
        return json;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private int code;
        private String msg;
        private String data;
    }

    private Result sendRequest(JSONObject jsonObject) {
        String chainIp = configuration.getChainCodeIp() + ChainCodeConstant.FABRIC_INVOKE;
        if (StringUtils.isBlank(chainIp)) {
            throw new BaseRuntimeException(CHAIN_CODE_LACK_CHAIN_CODE_IP);
        }

        log.info("请求数据[" + jsonObject.toString() + "]");

        //创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonObject.toJSONString(), headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(chainIp, httpEntity, String.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new BaseRuntimeException(CHAIN_CODE_REQUEST_ERROR);
        }

        return JSON.parseObject(responseEntity.getBody(), Result.class);
    }
}
