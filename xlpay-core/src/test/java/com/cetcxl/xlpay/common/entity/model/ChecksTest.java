package com.cetcxl.xlpay.common.entity.model;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ChecksTest {

    @Test
    @Disabled
    void appendInfo() {
        Checks checks = Checks.builder()
                .status(Checks.Status.APPLY)
                .build()
                .appendInfo("apply");

        System.out.println(checks.getInfo());
        System.out.println(checks.appendInfo("test").getInfo());

    }
}