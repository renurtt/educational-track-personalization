package com.nurtdinov.educationaltrackpersonalization.security.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import com.google.common.base.Strings;
import com.nurtdinov.educationaltrackpersonalization.security.dao.ApplicationUserDAO;
import com.nurtdinov.educationaltrackpersonalization.security.dto.MyHttpServletRequestWrapper;
import com.nurtdinov.educationaltrackpersonalization.security.exception.JwtTokenCanNotBeTrustedException;
import com.nurtdinov.educationaltrackpersonalization.security.jwt.JwsDecoder;
import com.nurtdinov.educationaltrackpersonalization.security.jwt.JwtConfig;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenVerifier extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Verifying JWS Token at endpoint" + request.getRequestURL().toString());
        String authorizationHeader = request.getHeader(jwtConfig.getAuthorizationHeader());
        if (Strings.isNullOrEmpty(authorizationHeader) || !authorizationHeader.startsWith(jwtConfig.getTokenPrefix())) {
            log.info("JWS Token is not provided in Authentication Header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.replace(jwtConfig.getTokenPrefix(), "");
        try {
            JwsDecoder decoder = new JwsDecoder(token, secretKey);
            decoder.decode();
            Claims body = decoder.getBody();
            String username = body.getSubject();

            var authorities = (List<Map<String, String>>) body.get("authorities");
            Set<SimpleGrantedAuthority> simpleGrantedAuthoritySet = authorities.stream()
                    .map(a -> new SimpleGrantedAuthority(a.get("authority")))
                    .collect(Collectors.toSet());
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, simpleGrantedAuthoritySet);
            SecurityContextHolder.getContext().setAuthentication(authentication);
//            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//            if (principal instanceof UserDetails) {
//              String username = ((UserDetails)principal).getUsername();
            MyHttpServletRequestWrapper wrappedRequest = new MyHttpServletRequestWrapper(request);
            wrappedRequest.addHeader("Username", getRequesterUsername(authorizationHeader));
            request = wrappedRequest;

            log.info("JWS Token is valid");
        } catch (RuntimeException e) {
            throw new JwtTokenCanNotBeTrustedException(token);
        }
        filterChain.doFilter(request, response);
    }

    public String getRequesterUsername(String authorizationHeader) {
        log.info("Getting requester username...");

        String token = authorizationHeader.replace(jwtConfig.getTokenPrefix(), "");
        JwsDecoder decoder = new JwsDecoder(token, secretKey);
        decoder.decode();
        String username = decoder.getBody().getSubject();

        log.info("Requester username = '" + username + "' provided by JWT Token from Authentication header '" + authorizationHeader + "'");

        return username;
    }


}
