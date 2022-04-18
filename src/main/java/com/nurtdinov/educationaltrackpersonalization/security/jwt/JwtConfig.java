package com.nurtdinov.educationaltrackpersonalization.security.jwt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@ConfigurationProperties(prefix = "application.jwt")
@Getter @Setter
@NoArgsConstructor
public class JwtConfig {

    private String secretKey;
    private String tokenPrefix; // NOTE: the trailing space should be omitted carefully!
    private Integer tokenExpirationAfterDays;

    public String getAuthorizationHeader() {
        return HttpHeaders.AUTHORIZATION;
    }
}
