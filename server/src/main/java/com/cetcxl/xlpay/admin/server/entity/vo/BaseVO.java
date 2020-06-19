package com.cetcxl.xlpay.admin.server.entity.vo;

import com.cetcxl.xlpay.admin.server.common.exception.BaseRuntimeException;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
@ApiModel
public abstract class BaseVO {
    public static <T extends BaseVO, M> T of(M model, Class<T> tClass) {
        try {
            T t = tClass.newInstance();
            BeanUtils.copyProperties(model, t);
            return t;
        } catch (Exception e) {
            throw new BaseRuntimeException(e);
        }
    }
}
