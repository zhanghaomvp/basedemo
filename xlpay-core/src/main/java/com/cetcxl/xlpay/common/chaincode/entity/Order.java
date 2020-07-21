package com.cetcxl.xlpay.common.chaincode.entity;

import com.cetcxl.xlpay.common.chaincode.enums.DealType;
import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @description: 订单信息
 * @author: henghuiwu
 * @date: 2020/7/10 15:03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Order extends BaseChainCodeModel {

    /**
     * 交易单号
     */
    @SignValue
    private String tradeNo;
    /**
     * 企业社会信用代码
     */
    private String companySocialCreditCode;
    /**
     * 员工身份证号
     */
    private String identityCard;
    /**
     * 员工钱包号
     */
    private String employeeWalletNo;
    /**
     * 商家社会信用代码
     */
    private String businessSocialCreditCode;
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

}
