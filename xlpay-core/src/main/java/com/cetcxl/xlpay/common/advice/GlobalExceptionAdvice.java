package com.cetcxl.xlpay.common.advice;

import com.cetcxl.xlpay.common.constants.IResultCode;
import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = BindException.class)
    public IResultCode handle(BindException e) {
        log.error("check system error : ", e);
        StringBuilder stringBuilder = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        fieldErrors.forEach(
                fieldError -> {
                    stringBuilder.append(fieldError.getField() + ":" + fieldError.getDefaultMessage() + System.lineSeparator());
                }
        );

        return IResultCode.DynamicResultCode
                .builder()
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .errorMessage(stringBuilder.toString())
                .build();
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public IResultCode handle(MethodArgumentNotValidException e) {
        log.error("check system error : ", e);
        StringBuilder stringBuilder = new StringBuilder();

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        fieldErrors.forEach(
                fieldError -> {
                    stringBuilder.append(fieldError.getField() + ":" + fieldError.getDefaultMessage() + System.lineSeparator());
                }
        );

        List<ObjectError> globalErrors = e.getBindingResult().getGlobalErrors();
        globalErrors.forEach(
                objectError -> {
                    stringBuilder.append(objectError.getDefaultMessage() + System.lineSeparator());
                }
        );

        return IResultCode.DynamicResultCode
                .builder()
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .errorMessage(stringBuilder.toString())
                .build();
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public IResultCode handle(ConstraintViolationException e) {
        log.error("check system error : ", e);
        StringBuilder stringBuilder = new StringBuilder();

        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        constraintViolations.forEach(
                constraintViolation -> {
                    stringBuilder.append(
                            constraintViolation.getInvalidValue() + ":" + constraintViolation.getMessage() + System.lineSeparator()
                    );
                }
        );
        return IResultCode.DynamicResultCode
                .builder()
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .errorMessage(stringBuilder.toString())
                .build();
    }

    @ExceptionHandler(value = BaseRuntimeException.class)
    public IResultCode handle(BaseRuntimeException e) {
        log.error("check system error : ", e);
        return e.getResultCode();
    }

    @ExceptionHandler(value = Exception.class)
    public IResultCode handle(Exception e) {
        log.error("check system error : ", e);
        return IResultCode.DynamicResultCode
                .builder()
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage(e.getMessage())
                .build();
    }
}
