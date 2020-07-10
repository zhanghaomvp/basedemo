package com.cetcxl.xlpay.common;

import com.cetcxl.xlpay.common.entity.model.CompanyStoreRelation;
import org.junit.jupiter.api.Test;

public class SomeTest {
    @Test
    void bitEnum() {
        System.out.println(CompanyStoreRelation.Relation.CREDIT_PAY.close(0));
        System.out.println(CompanyStoreRelation.Relation.CREDIT_PAY.close(1));
        System.out.println(CompanyStoreRelation.Relation.CREDIT_PAY.close(2));
        System.out.println(CompanyStoreRelation.Relation.CREDIT_PAY.close(3));

        System.out.println(CompanyStoreRelation.Relation.CASH_PAY.open(2));
        System.out.println(CompanyStoreRelation.Relation.CREDIT_PAY.open(2));
        System.out.println(CompanyStoreRelation.Relation.CASH_PAY.open(3));
        System.out.println(CompanyStoreRelation.Relation.CREDIT_PAY.open(0));
    }

}
