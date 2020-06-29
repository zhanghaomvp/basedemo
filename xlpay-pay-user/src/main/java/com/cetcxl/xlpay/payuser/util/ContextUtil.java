package com.cetcxl.xlpay.payuser.util;

import com.cetcxl.xlpay.payuser.service.UserDetailService;
import org.springframework.security.core.context.SecurityContextHolder;

public class ContextUtil {
    public static UserDetailService.PayUserInfo getUserInfo() {
        return (UserDetailService.PayUserInfo) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
