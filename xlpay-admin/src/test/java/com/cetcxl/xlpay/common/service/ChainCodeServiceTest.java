package com.cetcxl.xlpay.common.service;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.chaincode.entity.BusinessWallet;
import com.cetcxl.xlpay.common.chaincode.entity.CheckSlip;
import com.cetcxl.xlpay.common.chaincode.entity.Order;
import com.cetcxl.xlpay.common.chaincode.entity.PersonalWallet;
import com.cetcxl.xlpay.common.chaincode.enums.DealType;
import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @description:
 * @author: henghuiwu
 * @date: 2020/7/13 8:57
 */
@Slf4j
@Disabled
public class ChainCodeServiceTest extends BaseTest {
    @Autowired
    ChainCodeService chainCodeService;

    /**
     * 结算记录上链测试
     */
    @Test
    public void saveCheckSlip() {
        String checkSlipNo = "dbl1232451";
        List<String> tradeNos = Lists.newArrayList("b2c12167");

        CheckSlip checkSlip = CheckSlip.builder()
                                .checkNo(checkSlipNo)
                                .companySocialCreditCode("12123")
                                .businessSocialCreditCode("bba123214")
                                .totalAmount("36000.00")
                                .totalDeal("200")
                                .checkType(PayType.CREDIT)
                                .tradeNos(tradeNos)
                                .build();

        chainCodeService.saveCheckSlip(checkSlip);
    }

    /**
     * 交易记录上链测试
     */
    @Test
    public void saveDealingRecord() {
        String tradeNo = "b2c12168";
        String personalWalletNo = "1234432";
        String businessWalletNo = "87782011";
        String amount = "100.00";
        DealType dealType = DealType.CONSUME;
        PayType payType = PayType.CASH;

        Order order = Order.builder().tradeNo(tradeNo)
                        .companySocialCreditCode("12123")
                        .identityCard("da131231")
                        .employeeWalletNo(personalWalletNo)
                        .businessSocialCreditCode("bba123214")
                        .amount(amount)
                        .dealType(dealType)
                        .payType(payType)
                        .build();

        PersonalWallet personalWallet = PersonalWallet.builder()
                                        .personalWalletNo(personalWalletNo)
//                                        .personalCreditLimit("1000")
//                                        .personalCreditBalance("900")
                                        .personalCashBalance("900")
                                        .amount(amount)
                                        .dealType(dealType)
                                        .payType(payType)
                                        .tradeNo(tradeNo)
                                        .build();

        BusinessWallet businessWallet = BusinessWallet.builder()
                                        .businessSocialCreditCode(businessWalletNo)
                                        .amount(amount)
                                        .payType(payType)
                                        .tradeNo(tradeNo)
                                        .build();

        chainCodeService.saveDealingRecord(order, personalWallet, businessWallet);
    }



}
