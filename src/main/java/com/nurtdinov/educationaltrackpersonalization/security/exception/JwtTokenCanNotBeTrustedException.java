package com.nurtdinov.educationaltrackpersonalization.security.exception;

import com.nurtdinov.educationaltrackpersonalization.exception.RestException;
import org.springframework.http.HttpStatus;

public class JwtTokenCanNotBeTrustedException extends RestException {
    public JwtTokenCanNotBeTrustedException(String token) {
        super(HttpStatus.FORBIDDEN, "Token '" + token + "' can not be trusted");
    }
}
