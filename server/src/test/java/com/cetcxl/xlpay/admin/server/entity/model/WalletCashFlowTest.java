package com.cetcxl.xlpay.admin.server.entity.model;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class WalletCashFlowTest {

    @Test
    void caculateBalance() {
        WalletCashFlow cashFlow = WalletCashFlow.builder()
                .type(WalletCashFlow.CashFlowType.MINUS)
                .amount(new BigDecimal("100"))
                .build();

        cashFlow.caculateBalance(new BigDecimal("110.5"));
        Assert.assertTrue(cashFlow.getBalance().toString().equals("10.5"));
    }
}