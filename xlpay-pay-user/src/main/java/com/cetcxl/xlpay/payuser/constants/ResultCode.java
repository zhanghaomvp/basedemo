package com.cetcxl.xlpay.payuser.constants;


import com.cetcxl.xlpay.common.constants.IResultCode;

public enum ResultCode implements IResultCode {
    PAY_USER_EXIST(2001, "信链钱包支付已开通"),
    PAY_USER_NOT_EXIST(2002, "信链钱包支付已开通"),
    PAY_USER_PASSWORD_NOT_EXIST(2003, "原支付密码输入错误"),
    PAY_USER_NO_PASSWORD_PAY_NO_EXIST(2004, "你已经关闭免密支付成功"),
    PAY_USER_NO_PASSWORD_PAY_IS_EXIST(2004, "你已经开通免密支付成功"),

    COMPANY_NOT_EXIST(3001, "绑定企业不存在"),

    WALLET_BALANCE_NOT_ENOUGH(4001, "账户余额或剩余额度不足"),
    WALLET_RELATION_NOT_EXIST(4002, "企业未开通商家支付授信"),

    ;

    private int code;
    private String desc;

    ResultCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getErrorCode() {
        return this.code;
    }

    @Override
    public String getErrorMessage() {
        return this.desc;
    }
}
