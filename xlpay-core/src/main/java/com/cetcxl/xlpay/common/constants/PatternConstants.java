package com.cetcxl.xlpay.common.constants;


public interface PatternConstants {
    String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    String MUST_NUMBER = "^[0-9]*$";
    String PHONE = "^\\d{11,13}$";
    String VERIFY_CODE = "^\\d{6}$";
    String PAY_PASSWORD = "^\\d{6}$";
}
