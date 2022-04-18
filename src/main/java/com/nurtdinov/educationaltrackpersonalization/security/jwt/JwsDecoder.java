package com.nurtdinov.educationaltrackpersonalization.security.jwt;

import javax.crypto.SecretKey;

import com.nurtdinov.educationaltrackpersonalization.security.exception.JwtTokenCanNotBeTrustedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwsDecoder {

    @Getter
    private final String token;
    private final SecretKey secretKey;

    @Getter
    private Jws<Claims> jwsClaims;

    public void decode() {
        log.info("Decoding JWS token...");

        try {
            this.jwsClaims = Jwts.parserBuilder()
                    .setSigningKey(secretKey).build()
                    .parseClaimsJws(token);
        } catch (IllegalArgumentException | JwtException e) {
            throw new JwtTokenCanNotBeTrustedException(token);
        }
    }

    public Claims getBody() {
        return getJwsClaims().getBody();
    }

    public JwsHeader getHeader() {
        return getJwsClaims().getHeader();
    }

    public String getSignature() {
        return getJwsClaims().getSignature();
    }

}
