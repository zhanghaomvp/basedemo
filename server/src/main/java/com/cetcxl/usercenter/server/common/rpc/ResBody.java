package com.cetcxl.usercenter.server.common.rpc;

import com.cetcxl.usercenter.server.common.constants.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResBody<T> implements Serializable {
    private Status status;

    private T data;

    private String errorCode;
    private String errorMessage;

    public enum Status {
        OK,
        ERROR,
        ;
    }

    public static ResBody success(Object... o) {
        return ResBody.builder()
                .status(Status.OK)
                .data(o.length == 0 ? null : o)
                .build();
    }

    public static ResBody error(String errorCode, String errorMessage) {
        return ResBody.builder()
                .status(Status.ERROR)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    public static ResBody error(Object... o) {
        return ResBody.builder()
                .status(Status.ERROR)
                .data(o.length == 0 ? null : o)
                .build();
    }

    public static ResBody error(IResultCode resultCode) {
        return ResBody.builder()
                .status(Status.ERROR)
                .errorCode(resultCode.getErrorCode())
                .errorMessage(resultCode.getErrorMessage())
                .build();
    }
}
