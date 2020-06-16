package com.cetcxl.xlpay.admin.server.common.constants;

public enum CommonResultCode implements IResultCode {
    VERIFY_SIGN_HEAD_MISS("0001", "sign api head param miss"),
    VERIFY_SIGN_PUBLIC_KEY_MISS("0001", "sign api public key miss"),
    VERIFY_SIGN_FAIL("0001", "sign api check error"),

    XSTORE_UPLOAD_FAIL("0002", "xstore upload error"),
    XSTORE_DOWNLOAD_FAIL("0002", "xstore download error"),
    ;

    private String code;
    private String desc;

    CommonResultCode(String code, String desc) {
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
