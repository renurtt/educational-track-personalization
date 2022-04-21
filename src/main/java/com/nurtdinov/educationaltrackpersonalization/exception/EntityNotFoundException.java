package com.nurtdinov.educationaltrackpersonalization.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends RestException {

    public EntityNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
