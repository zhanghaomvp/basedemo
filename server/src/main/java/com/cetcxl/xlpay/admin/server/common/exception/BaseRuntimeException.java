package com.cetcxl.xlpay.admin.server.common.exception;

import com.cetcxl.xlpay.admin.server.common.constants.IResultCode;
import lombok.Getter;

@Getter
public class BaseRuntimeException extends RuntimeException {

    private IResultCode resultCode;

    public BaseRuntimeException(Throwable cause) {
        super(cause);
    }

    public BaseRuntimeException(Throwable cause, IResultCode resultCode) {
        super(cause);
        this.resultCode = resultCode;
    }
}
