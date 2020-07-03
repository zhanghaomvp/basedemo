package com.cetcxl.xlpay.admin.entity.model;

import com.cetcxl.xlpay.common.entity.model.WalletCashFlow;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class WalletCashFlowTest {

    @Test
    void caculateBalance() {
        WalletCashFlow cashFlow = WalletCashFlow.builder()
                .type(WalletCashFlow.CashFlowType.MINUS)
                .amount(new BigDecimal("100"))
                .balance(new BigDecimal("110.5"))
                .build();

        cashFlow.caculateBalance();
        Assert.assertTrue(cashFlow.getBalance().toString().equals("10.5"));
    }
}