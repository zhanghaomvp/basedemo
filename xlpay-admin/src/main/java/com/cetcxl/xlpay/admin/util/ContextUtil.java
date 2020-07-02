package com.cetcxl.xlpay.admin.util;

import com.cetcxl.xlpay.admin.service.UserDetailServiceImpl;
import org.springframework.security.core.context.SecurityContextHolder;

public class ContextUtil {
    public static UserDetailServiceImpl.UserInfo getUserInfo() {
        return (UserDetailServiceImpl.UserInfo) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
