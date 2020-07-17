package com.cetcxl.xlpay.payuser.service;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.fail;

class PayUserServiceTest extends BaseTest {
    @Autowired
    PayUserService payUserService;
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void payUser_lock_flow() throws Exception {
        PayUser payUser = payUserService.getById(2);

        Assertions.assertFalse(payUserService.isPayUserLocked(payUser));

        // 模拟输入5次错误密码
        payUserService.handlePasswordError(payUser);
        payUserService.handlePasswordError(payUser);
        payUserService.handlePasswordError(payUser);
        payUserService.handlePasswordError(payUser);

        LocalDateTime now = LocalDateTime.now();
        Thread.sleep(1000l);

        try {
            payUserService.handlePasswordError(payUser);
            fail();
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof BaseRuntimeException);
        }

        payUser = payUserService.getById(2);
        Assertions.assertTrue(payUserService.isPayUserLocked(payUser));
        Assertions.assertTrue(payUser.getStatus() == PayUser.PayUserStatus.LOCKED);
        MatcherAssert.assertThat(
                payUser.getLockedDeadLine().toEpochSecond(ZoneOffset.of("+8")),
                Matchers.greaterThan(now.plusHours(3).toEpochSecond(ZoneOffset.of("+8")))
        );

        payUserService
                .lambdaUpdate()
                .eq(PayUser::getId, 2)
                .set(PayUser::getLockedDeadLine, now)
                .update();

        payUserService.unlockPayUser();

        payUser = payUserService.getById(2);
        Assertions.assertTrue(payUser.getStatus() == PayUser.PayUserStatus.ACTIVE);

    }
}