package com.cetcxl.xlpay.common.chaincode.entity;

import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @description:
 * @author: henghuiwu
 * @date: 2020/7/15 14:15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckSlip extends BaseChainCodeModel {

    /**
     * 结算单号
     */
    @SignValue
    private String checkNo;
    /**
     * 企业社会信用代码
     */
    private String companySocialCreditCode;
    /**
     * 商家社会信用代码
     */
    private String businessSocialCreditCode;
    /**
     * 结算总金额
     */
    private String totalAmount;
    /**
     * 结算总条数
     */
    private String totalDeal;
    /**
     * 结算类型(信用/现金)
     */
    private PayType checkType;
    /**
     * 结算单关联交易单号
     */
    private List<String> tradeNos;

}
