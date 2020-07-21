package com.cetcxl.xlpay.common.constants;

public enum CommonResultCode implements IResultCode {
    SYSTEM_LOGIC_ERROR(9001, "系统错误"),
    RPC_ERROR(9002, "远程服务调用失败"),
    AUTHENTICATION_ERROR(9003, "验证失败 请重新登录"),
    SESSION_INVALID(9004, "已在其他地方重新登录 请确认"),
    LOGIN_FAIL(9005, "请确认用户名存在或者密码是否正确"),
    FILE_TYPE_NOT_SUPPORT(9006, "文件类型不支持"),
    REDIS_LOCK_ERROR(9007, "竞争激烈，请稍后再试"),
    VERIFY_CODE_UNAVAILABLE(9007, "竞争激烈，请稍后再试"),


    VERIFY_SIGN_HEAD_MISS(9101, "sign api head param miss"),
    VERIFY_SIGN_PUBLIC_KEY_MISS(9102, "sign api public key miss"),
    VERIFY_SIGN_FAIL(9103, "sign api check error"),

    XSTORE_UPLOAD_FAIL(9201, "xstore upload error"),
    XSTORE_DOWNLOAD_FAIL(9202, "xstore download error"),

    CHAIN_CODE_LACK_CHAIN_CODE_IP(9301, "缺少chainCodeIp"),
    CHAIN_CODE_REQUEST_ERROR(9302, "上链网络访问请求失败"),
    CHAIN_CODE_SAVE_DEALING_RECORD_ERROR(9303, "交易记录上链失败"),
    CHAIN_CODE_SAVE_CHECK_SLIP_ERROR(9304, "结算记录上链失败"),
    CHAIN_CODE_SIGN_ERROR(9305, "上链签名失败"),
    ;

    private int code;
    private String desc;

    CommonResultCode(int code, String desc) {
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
