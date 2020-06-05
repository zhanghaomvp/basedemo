package com.cetcxl.usercenter.server.constants;

import com.cetcxl.usercenter.server.common.constants.IResultCode;

public enum ResultCode implements IResultCode {

    USER_EXIST("1001", "该用户已存在");

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
