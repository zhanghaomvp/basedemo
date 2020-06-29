package com.cetcxl.xlpay.common.interceptor;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.cetcxl.xlpay.common.constants.CommonResultCode;
import com.cetcxl.xlpay.common.constants.IResultCode;
import com.cetcxl.xlpay.common.interceptor.annotation.SignApi;
import com.cetcxl.xlpay.common.rpc.ResBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class SignApiInterceptor implements HandlerInterceptor {
    public static final String SIGN_APP = "sign-app";
    public static final String SIGN_SALT = "sign-salt";
    public static final String SIGN = "sign";

    @Value("#{${http.sign-api.public-key-map}}")
    private Map<String, String> publicKeyMap;

    @Autowired
    private ObjectMapper mapper;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        SignApi signApi = ((HandlerMethod) handler).getMethodAnnotation(SignApi.class);
        if (Objects.isNull(signApi)) {
            return true;
        }

        String app = request.getHeader(SIGN_APP);
        String sign = request.getHeader(SIGN);

        if (StringUtils.isBlank(app) ||
                StringUtils.isBlank(sign)) {
            resolveResponse(response, CommonResultCode.VERIFY_SIGN_HEAD_MISS);
            return false;
        }

        String publicKey = publicKeyMap.get(app);
        if (StringUtils.isBlank(publicKey)) {
            resolveResponse(response, CommonResultCode.VERIFY_SIGN_PUBLIC_KEY_MISS);
            return false;
        }

        String buildParam = buildParam(request);
        if (StringUtils.isBlank(buildParam)) {
            buildParam = request.getHeader(SIGN_SALT);
        }

        Sign _verify = SecureUtil.sign(SignAlgorithm.SHA1withRSA, null, publicKey);

        boolean verify = _verify.verify(buildParam.getBytes(StandardCharsets.UTF_8), Base64.getDecoder().decode(sign));

        if (!verify) {
            resolveResponse(response, CommonResultCode.VERIFY_SIGN_FAIL);
            return false;
        }

        return true;
    }

    private String buildParam(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        List<String> list = Lists.newArrayList(parameterMap.keySet());
        Collections.sort(list, Comparator.naturalOrder());

        StringBuilder sb = new StringBuilder();
        list.forEach(
                str -> {
                    String s = parameterMap.get(str)[0];
                    if (StringUtils.isBlank(s)) {
                        return;
                    }
                    sb.append(s);
                }
        );

        return sb.toString();
    }

    private void resolveResponse(HttpServletResponse res, IResultCode resultCode) throws IOException {
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter out = res.getWriter();
        out.write(mapper.writeValueAsString(ResBody.error(resultCode)));
        out.flush();
        out.close();
    }
}
