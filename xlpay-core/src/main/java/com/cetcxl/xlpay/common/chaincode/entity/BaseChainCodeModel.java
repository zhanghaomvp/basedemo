package com.cetcxl.xlpay.common.chaincode.entity;

import com.cetcxl.xlpay.common.chaincode.util.SM2AlgorithmUtil;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.cetcxl.xlpay.common.chaincode.constants.ChainCodeConstant.pk;
import static com.cetcxl.xlpay.common.chaincode.constants.ChainCodeConstant.sk;
import static com.cetcxl.xlpay.common.constants.CommonResultCode.CHAIN_CODE_SIGN_ERROR;
import static com.cetcxl.xlpay.common.constants.PatternConstants.DATE_TIME;

@Data
@Slf4j
public class BaseChainCodeModel {
    private String upk = pk;
    private String sign;
    /**
     * 创建时间
     */
    private String created;

    public void sign() {
        try {
            this.created = DateTimeFormatter.ofPattern(DATE_TIME).format(LocalDateTime.now());

            Field[] declaredFields = this.getClass().getDeclaredFields();
            for (Field field : declaredFields) {

                SignValue signValue = field.getAnnotation(SignValue.class);
                if (Objects.isNull(signValue)) {
                    continue;
                }

                field.setAccessible(true);
                this.sign = SM2AlgorithmUtil.sign(sk, (String) field.get(this));
                return;
            }

            throw new BaseRuntimeException(CHAIN_CODE_SIGN_ERROR);
        } catch (Exception e) {

            log.error("BaseChainCodeModel sign error {}", e);
            throw new BaseRuntimeException(CHAIN_CODE_SIGN_ERROR);
        }
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface SignValue {
    }
}
