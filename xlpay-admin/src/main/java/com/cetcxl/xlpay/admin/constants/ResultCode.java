package com.cetcxl.xlpay.admin.constants;

import com.cetcxl.xlpay.common.constants.IResultCode;

public enum ResultCode implements IResultCode {
    VERIFY_CODE_FAIL("2101", "验证码校验失败"),

    COMPANY_EXIST("2201", "该企业已存在"),
    COMPANY_NOT_EXIST("2202", "该企业不存在"),
    COMPANY_USER_EXIST("2203", "该手机号已绑定企业"),
    COMPANY_MEMBER_EXIST("2204", "该企业成员存在"),
    COMPANY_STORE_RELATION_EXIST("2205", "企业与该商家已绑定"),
    COMPANY_STORE_RELATION_APPROVING("2206", "企业商家授信审核中,等待商家确认"),
    COMPANY_MEMBER_WALLET_NOT_EXIST("2207", "企业成员钱包不存在"),


    STORE_USER_EXIST("2301", "该手机号已绑定商家"),
    STORE_EXIST("2302", "该商家已经被绑定"),
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
