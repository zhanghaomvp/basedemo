package com.cetcxl.xlpay.payuser.util;

import com.cetcxl.xlpay.payuser.service.UserDetailServiceImpl;
import org.springframework.security.core.context.SecurityContextHolder;

public class ContextUtil {
    public static UserDetailServiceImpl.PayUserInfo getUserInfo() {
        return (UserDetailServiceImpl.PayUserInfo) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
