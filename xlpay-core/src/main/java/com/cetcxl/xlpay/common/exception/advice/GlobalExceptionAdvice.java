package com.cetcxl.xlpay.common.exception.advice;

import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.common.rpc.ResBody;
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
    public ResBody handle(BindException e) {
        log.error("check system error : ", e);
        StringBuilder stringBuilder = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        fieldErrors.forEach(
                fieldError -> {
                    stringBuilder.append(fieldError.getField() + ":" + fieldError.getDefaultMessage() + System.lineSeparator());
                }
        );

        return ResBody.error(HttpStatus.BAD_REQUEST.value(), stringBuilder.toString());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResBody handle(MethodArgumentNotValidException e) {
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

        return ResBody.error(HttpStatus.BAD_REQUEST.value(), stringBuilder.toString());
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResBody handle(ConstraintViolationException e) {
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
        return ResBody.error(HttpStatus.BAD_REQUEST.value(), stringBuilder.toString());
    }

    @ExceptionHandler(value = BaseRuntimeException.class)
    public ResBody handle(BaseRuntimeException e) {
        log.error("check system error : ", e);
        return ResBody.error(e.getResultCode(), e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public ResBody handle(Exception e) {
        log.error("check system error : ", e);
        return ResBody.error();
    }
}
