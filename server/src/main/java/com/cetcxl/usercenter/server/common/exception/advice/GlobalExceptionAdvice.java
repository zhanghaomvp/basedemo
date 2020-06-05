package com.cetcxl.usercenter.server.common.exception.advice;

import com.cetcxl.usercenter.server.common.entity.ResBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResBody handle(MethodArgumentNotValidException e) {
        StringBuilder stringBuilder = new StringBuilder();

        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

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
}
