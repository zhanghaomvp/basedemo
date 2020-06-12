package com.cetcxl.usercenter.server.entity.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompanyTest {

    @Test
    void companyFuntion_test() {
        assertTrue(Company.CompanyFuntionEnum.MEMBER_PAY.isOpen(1));
        assertFalse(Company.CompanyFuntionEnum.MEMBER_PAY.isOpen(2));

        assertEquals(1, Company.CompanyFuntionEnum.MEMBER_PAY.addFuntion(0));
    }
}