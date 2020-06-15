package com.cetcxl.xlpay.admin.server.common.service;

import com.cetcxl.xlpay.admin.server.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmsServiceTest extends BaseTest {
    @Autowired
    SmsService smsService;

    @Test
    void sendSmsBySmsBao() throws UnsupportedEncodingException {
        assertTrue(
                smsService.sendSmsBySmsBao("17360026771", "您的验证码是abc")
        );
    }
}