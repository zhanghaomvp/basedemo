package com.cetcxl.xlpay.payuser.entity.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.cetcxl.xlpay.payuser.entity.model.PayUser.PayUserFuntion.NO_PASSWORD_PAY;

class PayUserTest {
    @Test
    void testPayUserFuntion() {
        int funtion = 0;

        funtion = NO_PASSWORD_PAY.open(funtion);
        Assertions.assertTrue(funtion == 1);
        Assertions.assertTrue(NO_PASSWORD_PAY.isOpen(funtion));

        funtion = NO_PASSWORD_PAY.close(funtion);
        Assertions.assertFalse(NO_PASSWORD_PAY.isOpen(funtion));
    }
}