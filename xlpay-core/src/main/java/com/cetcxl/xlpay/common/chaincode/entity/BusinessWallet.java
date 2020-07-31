package com.cetcxl.xlpay.common.chaincode.entity;

import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @description: 商家钱包
 * @author: henghuiwu
 * @date: 2020/7/10 15:37
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BusinessWallet extends BaseChainCodeModel {

    /**
     * 商家社会信用代码
     */
    @SignValue
    private String businessSocialCreditCode;
    /**
     * 商家信用余额
     */
    private String businessCreditBalance;
    /**
     * 商家现金余额
     */
    private String businessCashBalance;
    /**
     * 交易金额
     */
    private String amount;
    /**
     * 支付类型
     */
    private PayType payType;
    /**
     * 交易单号
     */
    private String tradeNo;

}
