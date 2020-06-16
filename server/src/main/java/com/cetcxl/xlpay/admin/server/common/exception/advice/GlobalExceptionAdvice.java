package com.cetcxl.xlpay.admin.server.common.exception.advice;

import com.cetcxl.xlpay.admin.server.common.exception.BaseRuntimeException;
import com.cetcxl.xlpay.admin.server.common.rpc.ResBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
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

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResBody handle(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        StringBuilder stringBuilder = new StringBuilder();
        fieldErrors.forEach(
                fieldError -> {
                    stringBuilder.append(fieldError.getField() + ":" + fieldError.getDefaultMessage() + System.lineSeparator());
                }
        );

        List<ObjectError> globalErrors = bindingResult.getGlobalErrors();
        globalErrors.forEach(
                objectError -> {
                    stringBuilder.append(objectError.getDefaultMessage() + System.lineSeparator());
                }
        );

        return ResBody.error(HttpStatus.BAD_REQUEST.name(), stringBuilder.toString());
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResBody handle(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();

        StringBuilder stringBuilder = new StringBuilder();

        constraintViolations.forEach(
                constraintViolation -> {
                    stringBuilder.append(
                            constraintViolation.getInvalidValue() + ":" + constraintViolation.getMessage() + System.lineSeparator()
                    );
                }
        );
        return ResBody.error(HttpStatus.BAD_REQUEST.name(), stringBuilder.toString());
    }

    @ExceptionHandler(value = BaseRuntimeException.class)
    public ResBody handle(BaseRuntimeException e) {
        return ResBody.error(e.getResultCode(), e.getMessage());
    }
}
