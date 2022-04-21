package com.nurtdinov.educationaltrackpersonalization.security.exception;

import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import org.springframework.http.HttpStatus;

public class JwtTokenWasNotProvidedException extends RestException {
    public JwtTokenWasNotProvidedException() {
        super(HttpStatus.FORBIDDEN, "Token was not provided in 'Authorization' Header");
    }
}
