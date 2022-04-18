package com.nurtdinov.educationaltrackpersonalization.security.exception;

import org.springframework.http.HttpStatus;

public class JwtTokenCanNotBeTrustedException extends RestException {
    public JwtTokenCanNotBeTrustedException(String token) {
        super(HttpStatus.FORBIDDEN, "Token '" + token + "' can not be trusted");
    }
}
