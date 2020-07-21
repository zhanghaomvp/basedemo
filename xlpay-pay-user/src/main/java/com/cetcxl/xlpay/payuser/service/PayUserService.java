package com.cetcxl.xlpay.payuser.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.payuser.constants.ResultCode;
import com.cetcxl.xlpay.payuser.dao.PayUserMapper;
import com.cetcxl.xlpay.payuser.entity.model.PayUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.cetcxl.xlpay.payuser.constants.ResultCode.PAY_USER_LOCKED;
import static com.cetcxl.xlpay.payuser.entity.model.PayUser.PayUserStatus.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-28
 */
@Service
@Slf4j
public class PayUserService extends ServiceImpl<PayUserMapper, PayUser> {
    public static final String KEY_PAY_PASSWORD_ERROR_COUNT = "xlpay.payuser.password.error.count.";
    public static final int PASSWORD_ERROR_COUNT_LIMIT = 5;
    private static final long EXPIRE_MINUTE = 30L;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PayService payService;

    public void checkPayValid(PayUser payUser, String password, BigDecimal decimal) {
        if (isPayUserLocked(payUser)) {
            throw new BaseRuntimeException(ResultCode.PAY_USER_LOCKED);
        }

        if (!payService.checkNoPasswordPayValid(payUser, decimal)) {
            if (!passwordEncoder.matches(password, payUser.getPassword())) {
                handlePasswordError(payUser);
                throw new BaseRuntimeException(ResultCode.PAY_USER_PASSWORD_NOT_CORRECT);
            }
        }
    }

    public boolean isPayUserLocked(PayUser payUser) {
        if (Objects.isNull(payUser.getLockedDeadLine())) {
            return false;
        }

        if (payUser.getLockedDeadLine()
                .isAfter(LocalDateTime.now())) {
            return true;
        }

        return false;
    }

    public void handlePasswordError(PayUser payUser) {
        String key = KEY_PAY_PASSWORD_ERROR_COUNT + payUser.getIcNo();
        Boolean hasKey = redisTemplate.hasKey(key);
        Long increment = redisTemplate.opsForValue()
                .increment(key);

        if (!hasKey) {
            redisTemplate.expire(key, EXPIRE_MINUTE, TimeUnit.MINUTES);
        }

        if (increment < PASSWORD_ERROR_COUNT_LIMIT) {
            return;
        }

        lambdaUpdate()
                .eq(PayUser::getId, payUser.getId())
                .set(PayUser::getLockedDeadLine, LocalDateTime.now().plusHours(3))
                .set(PayUser::getStatus, LOCKED)
                .update();

        redisTemplate.delete(key);
        throw new BaseRuntimeException(PAY_USER_LOCKED);
    }

    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 60 * 5)
    public void unlockPayUser() {
        LocalDateTime now = LocalDateTime.now();
        lambdaUpdate()
                .le(PayUser::getLockedDeadLine, now)
                .ne(PayUser::getStatus, DISABLE)
                .eq(PayUser::getStatus, LOCKED)
                .set(PayUser::getLockedDeadLine, null)
                .set(PayUser::getStatus, ACTIVE)
                .update();

        log.info("unlockPayUser LocalDateTime finish: {}", now);
    }

}
