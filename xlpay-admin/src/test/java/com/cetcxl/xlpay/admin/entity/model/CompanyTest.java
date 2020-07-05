package com.cetcxl.xlpay.admin.entity.model;

import com.cetcxl.xlpay.common.entity.model.Company;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static org.junit.jupiter.api.Assertions.*;

public class CompanyTest {
    @Test
    void test() {
        System.out.println(MICROSOFT_EXCEL.toString());
        BigDecimal decimal = new BigDecimal("0");
        BigDecimal add = decimal.add(new BigDecimal("200"));
        System.out.println(decimal.toString());
        System.out.println(add.toString());
    }

    @Test
    void companyFuntion_test() {
        assertTrue(Company.CompanyFuntion.MEMBER_PAY.isOpen(1));
        assertFalse(Company.CompanyFuntion.MEMBER_PAY.isOpen(2));

        assertEquals(1, Company.CompanyFuntion.MEMBER_PAY.open(0));
    }

    @Test
    void decimal_test() {
        BigDecimal decimal = new BigDecimal("-1");
        BigDecimal subtract = decimal.subtract(new BigDecimal("200"));
        assertTrue(subtract.signum() == -1);
    }
}