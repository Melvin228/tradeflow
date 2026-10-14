package com.melvin.tradeflow.user.api;

import com.melvin.tradeflow.user.application.UserEmailAlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserController.class)
public class UserApiExceptionHandler {

    @ExceptionHandler(UserEmailAlreadyRegisteredException.class)
    public ProblemDetail handleDuplicateEmail(UserEmailAlreadyRegisteredException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }


}
