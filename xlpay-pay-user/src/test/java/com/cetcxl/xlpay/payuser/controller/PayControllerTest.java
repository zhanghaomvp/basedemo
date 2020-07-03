package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.common.entity.model.WalletCredit;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.service.DealService;
import com.cetcxl.xlpay.payuser.service.WalletCashService;
import com.cetcxl.xlpay.payuser.service.WalletCreditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

import static com.cetcxl.xlpay.common.entity.model.Deal.DealType.CASH_DEAL;
import static com.cetcxl.xlpay.common.entity.model.Deal.DealType.CREDIT_DEAL;

class PayControllerTest extends BaseTest {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DealService dealService;
    @Autowired
    WalletCashService walletCashService;
    @Autowired
    WalletCreditService walletCreditService;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        setAuthentication(
                PayUser.builder()
                        .id(1)
                        .icNo(S_ICNO)
                        .build()
        );
    }

    @Test
    void getCashBalance() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/pay-user/{id}/company/{socialCreditCode}/wallet/cash",
                                        1, S_CETCXL)
                                .accept(MediaType.APPLICATION_JSON_UTF8)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data").value("10.00")
                );
    }

    @Test
    void getCreditBalance() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/pay-user/{id}/company/{socialCreditCode}/wallet/credit",
                                        1, S_CETCXL)
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("$.data").value("100.00")
                );
    }

    @Test
    void payCash() throws Exception {
        BigDecimal oldCashBalance = walletCashService.getById(1).getCashBalance();

        PayController.PayReq req = PayController.PayReq.builder()
                .amount("5")
                .storeId(1)
                .info(S_TEMP)
                .password(S_PAY_PASSWORD)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/{id}/wallet/cash/{walletId}/deal",
                                        2, 2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Deal deal = dealService.getOne(
                Wrappers.lambdaQuery(Deal.class)
                        .eq(Deal::getStore, 1)
                        .eq(Deal::getType, CASH_DEAL)
        );

        Assertions.assertTrue(S_TEMP.equals(deal.getInfo()));
        Assertions.assertTrue(CASH_DEAL == deal.getType());

        BigDecimal newCashBalance = walletCashService.getById(2).getCashBalance();
        Assertions.assertTrue(newCashBalance.add(new BigDecimal("5")).compareTo(oldCashBalance) == 0);
    }

    @Test
    void payCredit() throws Exception {
        WalletCredit oldWalletCredit = walletCreditService.getById(2);

        PayController.PayReq req = PayController.PayReq.builder()
                .amount("5")
                .storeId(1)
                .info(S_TEMP)
                .password(S_PAY_PASSWORD)
                .build();

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/{id}/wallet/credit/{walletId}/deal",
                                        2, 2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Deal deal = dealService.getOne(
                Wrappers.lambdaQuery(Deal.class)
                        .eq(Deal::getStore, 1)
                        .eq(Deal::getType, CREDIT_DEAL)
        );

        Assertions.assertTrue(S_TEMP.equals(deal.getInfo()));
        Assertions.assertTrue(Deal.PayType.CREDIT == deal.getPayType());

        WalletCredit newWalletCredit = walletCreditService.getById(2);
        Assertions.assertTrue(
                newWalletCredit.getCreditBalance()
                        .add(new BigDecimal("5"))
                        .compareTo(oldWalletCredit.getCreditBalance()) == 0
        );
    }
}