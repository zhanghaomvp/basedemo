package com.cetcxl.usercenter.server.common.entity;

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

    public static ResBody success(Object o) {
        return ResBody.builder()
                .status(Status.OK)
                .data(o)
                .build();
    }

    public static ResBody error(String errorCode, String errorMessage) {
        return ResBody.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .status(Status.ERROR)
                .build();
    }

    public static ResBody error(IResultCode resultCode) {
        return ResBody.builder()
                .errorCode(resultCode.getErrorCode())
                .errorMessage(resultCode.getErrorMessage())
                .status(Status.ERROR)
                .build();
    }
}
