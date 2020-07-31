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

import static com.cetcxl.xlpay.common.chaincode.enums.DealType.LIMIT_CHANGE;
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
public class ChainCodeCreditBussinessTest extends BaseTest {
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
    public void credit_main_business() {

        //设置初始额度
        String limitTradeNo = UUID.randomUUID().toString();
        String limitAmount = "100.5";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(limitTradeNo)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(limitAmount)
                        .dealType(LIMIT_CHANGE)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCreditBalance("100.5")
                        .personalCreditLimit(limitAmount)
                        .amount(limitAmount)
                        .dealType(LIMIT_CHANGE)
                        .payType(PayType.CREDIT)
                        .tradeNo(limitTradeNo)
                        .build(),
                null
        );

        Assert.assertEquals(
                "100.5",
                chainCodeService
                        .queryOrderInfo(limitTradeNo)
                        .getAmount()
        );

        Assert.assertEquals(
                "100.5",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditLimit()
        );

        //额度消费一
        String payTradeNo1 = UUID.randomUUID().toString();
        String payAmount1 = "10.5";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(payTradeNo1)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(payAmount1)
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCreditBalance("90")
                        .personalCreditLimit("100.5")
                        .amount(payAmount1)
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo1)
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .amount(payAmount1)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo1)
                        .build()
        );

        Assert.assertEquals(
                "10.5",
                chainCodeService
                        .queryOrderInfo(payTradeNo1)
                        .getAmount()
        );

        Assert.assertEquals(
                "100.5",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditLimit()
        );

        Assert.assertEquals(
                payTradeNo1,
                chainCodeService
                        .queryBusinessWalletInfo(payTradeNo1)
                        .getTradeNo()
        );
        Assert.assertEquals(
                "10.5",
                chainCodeService
                        .queryBusinessWalletInfo(businessSocialCreditCode)
                        .getBusinessCreditBalance()
        );

        //额度消费二
        String payTradeNo2 = UUID.randomUUID().toString();
        String payAmount2 = "40";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(payTradeNo2)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(payAmount2)
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCreditBalance("50")
                        .personalCreditLimit("100.5")
                        .amount(payAmount2)
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo2)
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .amount(payAmount2)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo2)
                        .build()
        );

        Assert.assertEquals(
                payAmount2,
                chainCodeService
                        .queryOrderInfo(payTradeNo2)
                        .getAmount()
        );

        Assert.assertEquals(
                "50",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditBalance()
        );

        Assert.assertEquals(
                "100.5",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditLimit()
        );
        Assert.assertEquals(
                "50.5",
                chainCodeService
                        .queryBusinessWalletInfo(businessSocialCreditCode)
                        .getBusinessCreditBalance()
        );

        //额度调整
        String limitTradeNo_ = UUID.randomUUID().toString();
        String limitAmount_ = "80.3";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(limitTradeNo_)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(limitAmount_)
                        .dealType(LIMIT_CHANGE)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCreditBalance("29.8")
                        .personalCreditLimit(limitAmount_)
                        .amount(limitAmount_)
                        .dealType(LIMIT_CHANGE)
                        .payType(PayType.CREDIT)
                        .tradeNo(limitTradeNo_)
                        .build(),
                null
        );


        Assert.assertEquals(
                limitAmount_,
                chainCodeService
                        .queryOrderInfo(limitTradeNo_)
                        .getAmount()
        );

        Assert.assertEquals(
                "29.8",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditBalance()
        );

        // 验证超额支付
        String payTradeNo_ = UUID.randomUUID().toString();
        String payAmount_ = "40";
        try {
            chainCodeService.saveDealingRecord(
                    Order.builder()
                            .tradeNo(payTradeNo_)
                            .companySocialCreditCode(companySocialCreditCode)
                            .identityCard(identityCard)
                            .amount(payAmount_)
                            .dealType(DealType.CONSUME)
                            .employeeWalletNo(employeeWalletNo)
                            .payType(PayType.CREDIT)
                            .build(),
                    PersonalWallet.builder()
                            .personalWalletNo(employeeWalletNo)
                            .personalCreditBalance("0")
                            .personalCreditLimit("80.3")
                            .amount(payAmount_)
                            .dealType(DealType.CONSUME)
                            .payType(PayType.CREDIT)
                            .tradeNo(payTradeNo_)
                            .build(),
                    BusinessWallet.builder()
                            .businessSocialCreditCode(businessSocialCreditCode)
                            .amount(payAmount_)
                            .payType(PayType.CREDIT)
                            .tradeNo(payTradeNo_)
                            .build()
            );

            fail();
        } catch (Exception e) {
            Assertions.assertTrue(
                    ((BaseRuntimeException) e).getResultCode() == CHAIN_CODE_SAVE_DEALING_RECORD_ERROR
            );
        }

        //额度消费三
        String payTradeNo3 = UUID.randomUUID().toString();
        String payAmount3 = "29.8";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(payTradeNo3)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(payAmount3)
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCreditBalance("0")
                        .personalCreditLimit("80.3")
                        .amount(payAmount3)
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo3)
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .amount(payAmount3)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo3)
                        .build()
        );
        Assert.assertEquals(
                "0",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditBalance()
        );
        Assert.assertEquals(
                "80.3",
                chainCodeService
                        .queryBusinessWalletInfo(businessSocialCreditCode)
                        .getBusinessCreditBalance()
        );

        // 信用结算
        String checkBatch = UUID.randomUUID().toString();
        chainCodeService.saveCheckSlip(
                CheckSlip.builder()
                        .checkNo(checkBatch)
                        .companySocialCreditCode(companySocialCreditCode)
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .totalDeal("2")
                        .totalAmount("50.5")
                        .checkType(PayType.CREDIT)
                        .tradeNos(Lists.newArrayList(payTradeNo1, payTradeNo2))
                        .build()
        );

        Assert.assertEquals(
                "50.5",
                chainCodeService
                        .queryCheckInfo(checkBatch)
                        .getTotalAmount()
        );
        Assert.assertEquals(
                "50.5",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditBalance()
        );

        // 重复结算验证
        try {
            chainCodeService.saveCheckSlip(
                    CheckSlip.builder()
                            .checkNo(checkBatch)
                            .companySocialCreditCode(companySocialCreditCode)
                            .businessSocialCreditCode(businessSocialCreditCode)
                            .totalDeal("2")
                            .totalAmount("50.5")
                            .checkType(PayType.CREDIT)
                            .tradeNos(Lists.newArrayList(payTradeNo1, payTradeNo2))
                            .build()
            );
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(
                    ((BaseRuntimeException) e).getResultCode() == CHAIN_CODE_SAVE_CHECK_SLIP_ERROR
            );
        }

        // 错误结算验证
        try {
            String checkBatch_ = UUID.randomUUID().toString();
            chainCodeService.saveCheckSlip(
                    CheckSlip.builder()
                            .checkNo(checkBatch_)
                            .companySocialCreditCode(companySocialCreditCode)
                            .businessSocialCreditCode(businessSocialCreditCode)
                            .totalDeal("2")
                            .totalAmount("80")
                            .checkType(PayType.CREDIT)
                            .tradeNos(Lists.newArrayList(payTradeNo2, payTradeNo3))
                            .build()
            );
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(
                    ((BaseRuntimeException) e).getResultCode() == CHAIN_CODE_SAVE_CHECK_SLIP_ERROR
            );
        }

        // 再次信用结算
        String checkBatch1 = UUID.randomUUID().toString();
        chainCodeService.saveCheckSlip(
                CheckSlip.builder()
                        .checkNo(checkBatch1)
                        .companySocialCreditCode(companySocialCreditCode)
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .totalDeal("1")
                        .totalAmount("29.8")
                        .checkType(PayType.CREDIT)
                        .tradeNos(Lists.newArrayList(payTradeNo3))
                        .build()
        );
        Assert.assertEquals(
                "80.3",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditBalance()
        );

        //额度恢复支付验证
        String payTradeNo4 = UUID.randomUUID().toString();
        String payAmount4 = "80.3";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(payTradeNo4)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(payAmount4)
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCreditBalance("0")
                        .personalCreditLimit("80.3")
                        .amount(payAmount4)
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo4)
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .amount(payAmount4)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo4)
                        .build()
        );

        Assert.assertEquals(
                payAmount4,
                chainCodeService
                        .queryOrderInfo(payTradeNo4)
                        .getAmount()
        );

        Assert.assertEquals(
                "0",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditBalance()
        );
        Assert.assertEquals(
                "80.3",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditLimit()
        );
        Assert.assertEquals(
                "160.6",
                chainCodeService
                        .queryBusinessWalletInfo(businessSocialCreditCode)
                        .getBusinessCreditBalance()
        );

        //额度调整
        String limitTradeNo__ = UUID.randomUUID().toString();
        String limitAmount__ = "60.2";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(limitTradeNo__)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(limitAmount__)
                        .dealType(LIMIT_CHANGE)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCreditBalance("-20.1")
                        .personalCreditLimit(limitAmount__)
                        .amount(limitAmount__)
                        .dealType(LIMIT_CHANGE)
                        .payType(PayType.CREDIT)
                        .tradeNo(limitTradeNo__)
                        .build(),
                null
        );
        Assert.assertEquals(
                "60.2",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditLimit()
        );

        // 错误结算验证
        try {
            String checkBatch_ = UUID.randomUUID().toString();
            chainCodeService.saveCheckSlip(
                    CheckSlip.builder()
                            .checkNo(checkBatch_)
                            .companySocialCreditCode(companySocialCreditCode)
                            .businessSocialCreditCode(businessSocialCreditCode)
                            .totalDeal("1")
                            .totalAmount("80.4")
                            .checkType(PayType.CREDIT)
                            .tradeNos(Lists.newArrayList(payTradeNo4))
                            .build()
            );
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(
                    ((BaseRuntimeException) e).getResultCode() == CHAIN_CODE_SAVE_CHECK_SLIP_ERROR
            );
        }
        Assert.assertEquals(
                "-20.1",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditBalance()
        );

        // 再次信用结算
        String checkBatch2 = UUID.randomUUID().toString();
        chainCodeService.saveCheckSlip(
                CheckSlip.builder()
                        .checkNo(checkBatch2)
                        .companySocialCreditCode(companySocialCreditCode)
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .totalDeal("1")
                        .totalAmount("80.3")
                        .checkType(PayType.CREDIT)
                        .tradeNos(Lists.newArrayList(payTradeNo4))
                        .build()
        );
        Assert.assertEquals(
                "60.2",
                chainCodeService
                        .queryPersonalWalletInfo(employeeWalletNo)
                        .getPersonalCreditBalance()
        );
    }
}
