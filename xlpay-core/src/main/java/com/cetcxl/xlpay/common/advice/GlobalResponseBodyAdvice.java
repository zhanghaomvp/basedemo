package com.cetcxl.xlpay.common.advice;

import com.cetcxl.xlpay.common.constants.IResultCode;
import com.cetcxl.xlpay.common.rpc.ResBody;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = {"com.cetcxl.xlpay"})
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ResBody) {
            return body;
        }

        if (body instanceof IResultCode) {
            return ResBody.error((IResultCode) body);
        }

        return ResBody.success(body);
    }
}
