package com.nurtdinov.educationaltrackpersonalization.security.api;

import com.nurtdinov.educationaltrackpersonalization.security.ApplicationUserRole;
import com.nurtdinov.educationaltrackpersonalization.security.dto.AuthenticationRequest;
import com.nurtdinov.educationaltrackpersonalization.security.dto.RegistrationRequest;
import com.nurtdinov.educationaltrackpersonalization.security.exception.JwtTokenWasNotProvidedException;
import com.nurtdinov.educationaltrackpersonalization.security.service.ApplicationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final ApplicationUserService applicationUserService;

    @PostMapping("/register/user")
    public void registerUser(@RequestBody RegistrationRequest registrationRequest) {
        log.info("POST /auth/register/user");

        applicationUserService.register(registrationRequest, ApplicationUserRole.USER);
    }

    @PostMapping("/register/editor")
    public void registerEditor(@RequestBody RegistrationRequest registrationRequest) {
        log.info("POST /auth/register/editor");

        applicationUserService.register(registrationRequest, ApplicationUserRole.EDITOR);
    }
//
//
    @GetMapping("/check/user")
    public void jwtCheckUser(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null)
            throw new JwtTokenWasNotProvidedException();
    }
//
//    @GetMapping("/check/editor")
//    public void jwtCheckAuthor(@RequestHeader("Authorization") String authorizationHeader) {
//        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
//            throw new JwtTokenWasNotProvidedException();
//    }

}
