package com.cetcxl.xlpay.common.service;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.chaincode.config.ChainCodeConfiguration;
import com.cetcxl.xlpay.common.chaincode.entity.BusinessWallet;
import com.cetcxl.xlpay.common.chaincode.entity.CheckSlip;
import com.cetcxl.xlpay.common.chaincode.entity.Order;
import com.cetcxl.xlpay.common.chaincode.entity.PersonalWallet;
import com.cetcxl.xlpay.common.chaincode.enums.DealType;
import com.cetcxl.xlpay.common.chaincode.enums.PayType;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.cetcxl.xlpay.common.constants.CommonResultCode.CHAIN_CODE_SAVE_CHECK_SLIP_ERROR;
import static com.cetcxl.xlpay.common.constants.CommonResultCode.CHAIN_CODE_SAVE_DEALING_RECORD_ERROR;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @description:
 * @author: henghuiwu
 * @date: 2020/7/13 8:57
 */
@Slf4j
//@Disabled
public class ChainCodeCashBussinessTest extends BaseTest {
    private final static String PREFIX = "ChainCodeTest.";
    @Autowired
    private ChainCodeConfiguration configuration;
    @Autowired
    ChainCodeService chainCodeService;

    private String companySocialCreditCode;
    private String businessSocialCreditCode;
    private String identityCard;
    private String employeeWalletNo;

    {
        String s = PREFIX + UUID.randomUUID().toString();
        companySocialCreditCode = s + ".c";
        businessSocialCreditCode = s + ".b";
        identityCard = s;
        employeeWalletNo = s + ".w";
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        configuration.setChainCodeIp("http://172.16.5.27/cc_manager");
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
        configuration.setChainCodeIp("http://127.0.0.1:8089/cc_manager");
    }

    @Test
    public void cash_main_business() {
        //充值
        String rechargeTradeNo = UUID.randomUUID().toString();
        String rechargeAmount = "100.5";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(rechargeTradeNo)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(rechargeAmount)
                        .dealType(DealType.RECHARGE)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CASH)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCashBalance("100.5")
                        .amount(rechargeAmount)
                        .dealType(DealType.RECHARGE)
                        .payType(PayType.CASH)
                        .tradeNo(rechargeTradeNo)
                        .build(),
                null
        );

        Assert.assertEquals(
                rechargeAmount,
                chainCodeService
                        .queryOrderInfo(rechargeTradeNo)
                        .getAmount()
        );

        Assert.assertEquals(
                rechargeAmount,
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCashBalance()
        );

        //扣减
        String reduceTradeNo = UUID.randomUUID().toString();
        String reduceAmount = "-0.3";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(reduceTradeNo)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(reduceAmount)
                        .dealType(DealType.RECHARGE)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CASH)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCashBalance("100.2")
                        .amount(reduceAmount)
                        .dealType(DealType.RECHARGE)
                        .payType(PayType.CASH)
                        .tradeNo(reduceTradeNo)
                        .build(),
                null
        );

        Assert.assertEquals(
                "-0.3",
                chainCodeService
                        .queryOrderInfo(reduceTradeNo)
                        .getAmount()
        );

        Assert.assertEquals(
                "100.2",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCashBalance()
        );


        //消费
        String payTradeNo = UUID.randomUUID().toString();
        String payAmount = "50";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(payTradeNo)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(payAmount)
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CASH)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCashBalance("50.2")
                        .amount(payAmount)
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CASH)
                        .tradeNo(payTradeNo)
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .amount(payAmount)
                        .payType(PayType.CASH)
                        .tradeNo(payTradeNo)
                        .build()
        );

        Assert.assertEquals(
                "50",
                chainCodeService
                        .queryOrderInfo(payTradeNo)
                        .getAmount()
        );
        Assert.assertEquals(
                "50.2",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCashBalance()
        );
        Assert.assertEquals(
                "50",
                chainCodeService
                        .queryBusinessWalletInfo(businessSocialCreditCode)
                        .getBusinessCashBalance()
        );

        //验证超额支付
        try {
            String payTradeNo_ = UUID.randomUUID().toString();
            String payAmount_ = "100";
            chainCodeService.saveDealingRecord(
                    Order.builder()
                            .tradeNo(payTradeNo_)
                            .companySocialCreditCode(companySocialCreditCode)
                            .identityCard(identityCard)
                            .amount(payAmount_)
                            .dealType(DealType.CONSUME)
                            .employeeWalletNo(employeeWalletNo)
                            .payType(PayType.CASH)
                            .build(),
                    PersonalWallet.builder()
                            .personalWalletNo(employeeWalletNo)
                            .personalCashBalance("-50")
                            .amount(payAmount_)
                            .dealType(DealType.CONSUME)
                            .payType(PayType.CASH)
                            .tradeNo(payTradeNo_)
                            .build(),
                    BusinessWallet.builder()
                            .businessSocialCreditCode(businessSocialCreditCode)
                            .amount(payAmount_)
                            .payType(PayType.CASH)
                            .tradeNo(payTradeNo_)
                            .build()
            );
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(
                    ((BaseRuntimeException) e).getResultCode() == CHAIN_CODE_SAVE_DEALING_RECORD_ERROR
            );
        }

        //验证错误支付
        try {
            String payTradeNo__ = UUID.randomUUID().toString();
            String payAmount__ = "50";
            chainCodeService.saveDealingRecord(
                    Order.builder()
                            .tradeNo(payTradeNo__)
                            .companySocialCreditCode(companySocialCreditCode)
                            .identityCard(identityCard)
                            .amount(payAmount__)
                            .dealType(DealType.CONSUME)
                            .employeeWalletNo(employeeWalletNo)
                            .payType(PayType.CASH)
                            .build(),
                    PersonalWallet.builder()
                            .personalWalletNo(employeeWalletNo)
                            .personalCashBalance("5")
                            .amount(payAmount__)
                            .dealType(DealType.CONSUME)
                            .payType(PayType.CASH)
                            .tradeNo(payTradeNo__)
                            .build(),
                    BusinessWallet.builder()
                            .businessSocialCreditCode(businessSocialCreditCode)
                            .amount(payAmount__)
                            .payType(PayType.CASH)
                            .tradeNo(payTradeNo__)
                            .build()
            );
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(
                    ((BaseRuntimeException) e).getResultCode() == CHAIN_CODE_SAVE_DEALING_RECORD_ERROR
            );
        }
        Assertions.assertEquals(
                "50.2",
                chainCodeService.queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCashBalance()
        );

        //验证再次成功消费
        String payTradeNo___ = UUID.randomUUID().toString();
        String payAmount___ = "50.0";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(payTradeNo___)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(payAmount___)
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CASH)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCashBalance("0.2")
                        .amount(payAmount___)
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CASH)
                        .tradeNo(payTradeNo___)
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .amount(payAmount___)
                        .payType(PayType.CASH)
                        .tradeNo(payTradeNo___)
                        .build()
        );
        Assertions.assertEquals(
                "0.2",
                chainCodeService.queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCashBalance()
        );
        Assertions.assertEquals(
                "50.0",
                chainCodeService.queryOrderInfo(payTradeNo___)
                        .getAmount()
        );
        Assert.assertEquals(
                "100",
                chainCodeService
                        .queryBusinessWalletInfo(businessSocialCreditCode)
                        .getBusinessCashBalance()
        );

        // 错误结算验证
        try {
            String checkBatch_ = UUID.randomUUID().toString();
            chainCodeService.saveCheckSlip(
                    CheckSlip.builder()
                            .checkNo(checkBatch_)
                            .companySocialCreditCode(companySocialCreditCode)
                            .businessSocialCreditCode(businessSocialCreditCode)
                            .totalDeal("2")
                            .totalAmount("100.1")
                            .checkType(PayType.CASH)
                            .tradeNos(Lists.newArrayList(payTradeNo, payTradeNo___))
                            .build()
            );
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(
                    ((BaseRuntimeException) e).getResultCode() == CHAIN_CODE_SAVE_CHECK_SLIP_ERROR
            );
        }

        //结算
        String checkBatch = UUID.randomUUID().toString();
        chainCodeService.saveCheckSlip(
                CheckSlip.builder()
                        .checkNo(checkBatch)
                        .companySocialCreditCode(companySocialCreditCode)
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .totalDeal("2")
                        .totalAmount("100")
                        .checkType(PayType.CASH)
                        .tradeNos(Lists.newArrayList(payTradeNo, payTradeNo___))
                        .build()
        );

        Assert.assertEquals(
                "100",
                chainCodeService
                        .queryCheckInfo(checkBatch)
                        .getTotalAmount()
        );

        //再次充值
        String rechargeTradeNo_ = UUID.randomUUID().toString();
        String rechargeAmount_ = "10.5";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(rechargeTradeNo_)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(rechargeAmount_)
                        .dealType(DealType.RECHARGE)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CASH)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCashBalance("10.7")
                        .amount(rechargeAmount_)
                        .dealType(DealType.RECHARGE)
                        .payType(PayType.CASH)
                        .tradeNo(rechargeTradeNo_)
                        .build(),
                null
        );
        Assert.assertEquals(
                "10.7",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCashBalance()
        );


        //充值完再消费
        String payTradeNo____ = UUID.randomUUID().toString();
        String payAmount____ = "10.5";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(payTradeNo____)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(payAmount____)
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CASH)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCashBalance("0.2")
                        .amount(payAmount____)
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CASH)
                        .tradeNo(payTradeNo____)
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .amount(payAmount____)
                        .payType(PayType.CASH)
                        .tradeNo(payTradeNo____)
                        .build()
        );
        Assert.assertEquals(
                "0.2",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCashBalance()
        );
        Assert.assertEquals(
                "110.5",
                chainCodeService
                        .queryBusinessWalletInfo(businessSocialCreditCode)
                        .getBusinessCashBalance()
        );

    }

}
