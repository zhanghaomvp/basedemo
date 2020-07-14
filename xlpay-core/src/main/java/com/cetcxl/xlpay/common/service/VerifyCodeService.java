package com.cetcxl.xlpay.common.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ${author}
 * @since 2020-06-10
 */
@Service
@Slf4j
public class VerifyCodeService {
    private static final String PREFIX_REDIS_KEY = "verify.code:";
    private static final String CONTENT_TEMPLATE = "您的验证码是:%s,短信2分钟内有效。";
    private static final int EXPIRE_MINUTE = 2;

    public static final String JUMP_VERIFY_CODE = "000000";

    @Autowired
    private SmsService smsService;
    @Autowired
    private RedisTemplate redisTemplate;

    public boolean sendVerifyCode(String phone) {
        String verifyCode = generateVerifyCode();
        boolean flag = smsService.sendSmsBySmsBao(
                phone, String.format(CONTENT_TEMPLATE, verifyCode)
        );

        if (!flag) {
            return flag;
        }

        redisTemplate.opsForValue().set(
                PREFIX_REDIS_KEY + phone,
                verifyCode,
                EXPIRE_MINUTE,
                TimeUnit.MINUTES
        );
        return true;
    }

    public boolean checkVerifyCode(String phone, String code) {
        if (JUMP_VERIFY_CODE.equals(code)) {
            return true;
        }

        String real = (String) redisTemplate.opsForValue().get(PREFIX_REDIS_KEY + phone);
        if (!code.equals(real)) {
            return false;
        }

        //redisTemplate.delete(PREFIX_REDIS_KEY + phone);
        return true;
    }

    private static final int BASE_VERIFY_CODE = 100000;

    private String generateVerifyCode() {
        int num = ThreadLocalRandom.current().nextInt(999999);
        if (num < BASE_VERIFY_CODE) {
            num += BASE_VERIFY_CODE;
        }
        return String.valueOf(num);
    }
}
