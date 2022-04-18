package com.nurtdinov.educationaltrackpersonalization.security.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyRegisteredException extends RestException {

    public UserAlreadyRegisteredException(String msg) {
        super(HttpStatus.CONFLICT, "'" + msg + "' is already registered username");
    }
}
