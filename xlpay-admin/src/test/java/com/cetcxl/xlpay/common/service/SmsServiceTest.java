package com.cetcxl.xlpay.common.service;

import com.cetcxl.xlpay.admin.BaseTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmsServiceTest extends BaseTest {
    @Autowired
    SmsService smsService;

    @Test
    void sendSmsBySmsBao_success() {
        WireMock.stubFor(
                WireMock
                        .get(
                                WireMock
                                        .urlPathEqualTo("/sms")
                        )
                        .willReturn(
                                WireMock
                                        .okJson("0")
                        )

        );

        assertTrue(smsService.sendSmsBySmsBao("17360026771", "您的验证码是abcd"));
        WireMock.verify(exactly(1), WireMock.getRequestedFor(WireMock.urlPathEqualTo("/sms")));
    }
}