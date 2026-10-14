package com.melvin.tradeflow.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalApiExceptionHandler {
    private static final String VALIDATION_ERROR_CODE = "REQUEST_VALIDATION_FAILED";
    private static final String ERROR_CODE_PROPERTY = "code";
    private static final String ERRORS_PROPERTY = "errors";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidRequest(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more request fields are invalid."
        );

        problem.setTitle("Request validation failed");
        problem.setProperty(ERROR_CODE_PROPERTY, VALIDATION_ERROR_CODE);
        problem.setProperty(ERRORS_PROPERTY, errors);

        return problem;
    }
}
