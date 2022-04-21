package com.nurtdinov.educationaltrackpersonalization.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class RegistrationRequest {

    private String username;
    private String password;

}