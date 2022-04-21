package com.nurtdinov.educationaltrackpersonalization.security.exception;

import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import org.springframework.http.HttpStatus;

public class UserAlreadyRegisteredException extends RestException {

    public UserAlreadyRegisteredException(String msg) {
        super(HttpStatus.CONFLICT, "'" + msg + "' is already registered username");
    }
}
