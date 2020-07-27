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
import org.junit.jupiter.api.*;
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
public class ChainCodeBussinessTest extends BaseTest {
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
    @Disabled
    public void queryPersonalWallet() {
        PersonalWallet personalWallet = chainCodeService.queryPersonalWalletInfo(employeeWalletNo);
        log.info("result data:[{}]", personalWallet);
    }

    @Test
    public void cash_main_bussiness() {
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

        //扣减
        String reduceTradeNo = UUID.randomUUID().toString();
        String reduceAmount = "-0.5";
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
                        .personalCashBalance("100")
                        .amount(reduceAmount)
                        .dealType(DealType.RECHARGE)
                        .payType(PayType.CASH)
                        .tradeNo(reduceTradeNo)
                        .build(),
                null
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
                        .personalCashBalance("50")
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

        //结算
        String checkBatch = UUID.randomUUID().toString();
        chainCodeService.saveCheckSlip(
                CheckSlip.builder()
                        .checkNo(checkBatch)
                        .companySocialCreditCode(companySocialCreditCode)
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .totalDeal("1")
                        .totalAmount("50")
                        .checkType(PayType.CASH)
                        .tradeNos(Lists.newArrayList(payTradeNo))
                        .build()
        );
    }

    @Test
    public void credit_main_bussiness() {

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

        //额度调整
        String limitTradeNo_ = UUID.randomUUID().toString();
        String limitAmount_ = "80.5";
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
                        .personalCreditBalance("30")
                        .personalCreditLimit(limitAmount_)
                        .amount(limitAmount_)
                        .dealType(LIMIT_CHANGE)
                        .payType(PayType.CREDIT)
                        .tradeNo(limitTradeNo_)
                        .build(),
                null
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
                            .personalCreditLimit("80.5")
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

        // 信用结算
        String checkBatch = UUID.randomUUID().toString();
        chainCodeService.saveCheckSlip(
                CheckSlip.builder()
                        .checkNo(checkBatch)
                        .companySocialCreditCode(companySocialCreditCode)
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .totalDeal("2")
                        .totalAmount("50")
                        .checkType(PayType.CREDIT)
                        .tradeNos(Lists.newArrayList(payTradeNo1, payTradeNo2))
                        .build()
        );

        // 重复结算验证
        try {
            chainCodeService.saveCheckSlip(
                    CheckSlip.builder()
                            .checkNo(checkBatch)
                            .companySocialCreditCode(companySocialCreditCode)
                            .businessSocialCreditCode(businessSocialCreditCode)
                            .totalDeal("2")
                            .totalAmount("50")
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

        //额度恢复支付验证
        String payTradeNo__ = UUID.randomUUID().toString();
        String payAmount__ = "80.5";
        chainCodeService.saveDealingRecord(
                Order.builder()
                        .tradeNo(payTradeNo__)
                        .companySocialCreditCode(companySocialCreditCode)
                        .identityCard(identityCard)
                        .amount(payAmount__)
                        .dealType(DealType.CONSUME)
                        .employeeWalletNo(employeeWalletNo)
                        .payType(PayType.CREDIT)
                        .build(),
                PersonalWallet.builder()
                        .personalWalletNo(employeeWalletNo)
                        .personalCreditBalance("0")
                        .personalCreditLimit("80.5")
                        .amount(payAmount__)
                        .dealType(DealType.CONSUME)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo__)
                        .build(),
                BusinessWallet.builder()
                        .businessSocialCreditCode(businessSocialCreditCode)
                        .amount(payAmount__)
                        .payType(PayType.CREDIT)
                        .tradeNo(payTradeNo__)
                        .build()
        );
    }

}
