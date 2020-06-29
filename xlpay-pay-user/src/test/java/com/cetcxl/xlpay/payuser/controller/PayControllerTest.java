package com.cetcxl.xlpay.payuser.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.entity.model.Deal;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import com.cetcxl.xlpay.payuser.service.DealService;
import com.cetcxl.xlpay.payuser.service.WalletCashService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;

class PayControllerTest extends BaseTest {
    @Autowired
    DealService dealService;
    @Autowired
    WalletCashService walletCashService;

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

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/pay-user/{id}/wallet/cash/{walletId}/deal",
                                        2, 2)
                                .param("storeId", "1")
                                .param("amount", "5")
                                .param("info", S_TEMP)
                                .param("password", S_PAY_PASSWORD)
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Deal deal = dealService.getOne(
                Wrappers.lambdaQuery(Deal.class)
                        .eq(Deal::getStore, 1)
        );

        Assertions.assertTrue(S_TEMP.equals(deal.getInfo()));
        Assertions.assertTrue(Deal.DealType.CASH_DEAL == deal.getType());

        BigDecimal newCashBalance = walletCashService.getById(2).getCashBalance();
        Assertions.assertTrue(newCashBalance.add(new BigDecimal("5")).compareTo(oldCashBalance) == 0);
    }
}