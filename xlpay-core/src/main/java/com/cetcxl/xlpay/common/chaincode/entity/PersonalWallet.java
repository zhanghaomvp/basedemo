package com.cetcxl.xlpay.common.chaincode.entity;

import com.cetcxl.xlpay.common.chaincode.enums.DealType;
import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @description: 个人钱包
 * @author: henghuiwu
 * @date: 2020/7/10 15:37
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PersonalWallet extends BaseChainCodeModel {

    /**
     * 个人钱包号
     */
    @SignValue
    private String personalWalletNo;
    /**
     * 个人信用额度
     */
    private String personalCreditLimit;
    /**
     * 个人信用余额
     */
    private String personalCreditBalance;
    /**
     * 个人现金余额
     */
    private String personalCashBalance;
    /**
     * 交易金额
     */
    private String amount;
    /**
     * 交易类型
     */
    private DealType dealType;
    /**
     * 支付类型
     */
    private PayType payType;
    /**
     * 交易单号
     */
    private String tradeNo;
}
