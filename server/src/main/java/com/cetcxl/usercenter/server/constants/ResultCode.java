package com.cetcxl.usercenter.server.constants;

import com.cetcxl.usercenter.server.common.constants.IResultCode;

public enum ResultCode implements IResultCode {
    VERIFY_CODE_FAIL("1001", "验证码校验失败"),
    COMPANY_EXIST("2001", "该企业已存在"),
    COMPANY_NOT_EXIST("2002", "该企业不存在"),
    ;

    private String code;
    private String desc;

    ResultCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getErrorCode() {
        return this.code;
    }

    @Override
    public String getErrorMessage() {
        return this.desc;
    }
}
