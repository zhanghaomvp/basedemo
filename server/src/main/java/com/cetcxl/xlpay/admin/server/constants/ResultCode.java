package com.cetcxl.xlpay.admin.server.constants;

import com.cetcxl.xlpay.admin.server.common.constants.IResultCode;

public enum ResultCode implements IResultCode {
    VERIFY_CODE_FAIL("2101", "验证码校验失败"),

    COMPANY_EXIST("2201", "该企业已存在"),
    COMPANY_NOT_EXIST("2202", "该企业不存在"),

    COMPANY_MEMBER_EXIST("2301", "该企业成员存在"),
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
