package com.cetcxl.xlpay.admin.entity.model;

import com.cetcxl.xlpay.common.entity.model.Company;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompanyTest {

    @Test
    void companyFuntion_test() {
        assertTrue(Company.CompanyFuntion.MEMBER_PAY.isOpen(1));
        assertFalse(Company.CompanyFuntion.MEMBER_PAY.isOpen(2));

        assertEquals(1, Company.CompanyFuntion.MEMBER_PAY.addFuntion(0));
    }
}