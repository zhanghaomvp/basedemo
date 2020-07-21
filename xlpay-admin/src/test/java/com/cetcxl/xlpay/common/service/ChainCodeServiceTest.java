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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @description:
 * @author: henghuiwu
 * @date: 2020/7/13 8:57
 */
@Slf4j
public class ChainCodeServiceTest extends BaseTest {
    @Autowired
    ChainCodeService chainCodeService;

    /**
     * 结算记录上链测试
     *
     * @throws Exception
     */
    @Test
    public void saveCheckSlip() throws Exception {
        String checkSlipNo = "dbl123245";
        List<String> tradeNos = Lists.newArrayList("b2c123166", "b2c123167", "b2c123168", "b2c123169");

        CheckSlip checkSlip = new CheckSlip()
                .setCheckNo(checkSlipNo)
                .setCompanySocialCreditCode("12123")
                .setBusinessSocialCreditCode("bba123214")
                .setTotalAmount("36000.00")
                .setTotalDeal("200")
                .setCheckType(PayType.CREDIT)
                .setTradeNos(tradeNos);

        chainCodeService.saveCheckSlip(checkSlip);
    }

    /**
     * 交易记录上链测试
     *
     * @throws Exception
     */
    @Test
    public void saveDealingRecord() {
        String tradeNo = "b2c123166";
        String personalWalletNo = "123156";
        String businessWalletNo = "87782011";

        Order order = new Order()
                .setTradeNo(tradeNo)
                .setCompanySocialCreditCode("12123")
                .setIdentityCard("da131231")
                .setBusinessSocialCreditCode("bba123214")
                .setAmount("100.00")
                .setDealType(DealType.CONSUME)
                .setPayType(PayType.CREDIT);

        PersonalWallet personalWallet = new PersonalWallet()
                .setPersonalWalletNo(personalWalletNo)
                .setPersonalCreditLimit("400.00")
                .setPersonalCreditBalance("100.00")
                .setPersonalCashBalance("0.00")
                .setAmount("100.00")
                .setDealType(DealType.CONSUME)
                .setPayType(PayType.CREDIT)
                .setTradeNo(tradeNo);

        BusinessWallet businessWallet = new BusinessWallet()
                .setBusinessSocialCreditCode(businessWalletNo)
                .setAmount("100.00")
                .setPayType(PayType.CREDIT)
                .setTradeNo(tradeNo);

        chainCodeService.saveDealingRecord(order, personalWallet, businessWallet);
    }

}
