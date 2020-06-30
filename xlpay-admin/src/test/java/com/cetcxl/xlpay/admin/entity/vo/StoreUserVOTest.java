package com.cetcxl.xlpay.admin.entity.vo;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.entity.model.StoreUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StoreUserVOTest extends BaseTest {
    @Test
    void vo_of_success() {
        StoreUser storeUser = StoreUser.builder()
                .phone(S_PHONE)
                .id(1)
                .created(LocalDateTime.now())
                .build();

        StoreUserVO storeUserVO = StoreUserVO.of(storeUser, StoreUserVO.class);
        assertTrue(S_PHONE.equals(storeUserVO.getPhone()));
    }

}