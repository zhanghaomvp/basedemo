package com.cetcxl.xlpay.admin.util;

import com.cetcxl.xlpay.admin.service.UserDetailService;
import org.springframework.security.core.context.SecurityContextHolder;

public class ContextUtil {
    public static UserDetailService.UserInfo getUserInfo() {
        return (UserDetailService.UserInfo) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
