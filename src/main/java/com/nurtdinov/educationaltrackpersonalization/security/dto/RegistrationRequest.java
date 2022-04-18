package com.nurtdinov.educationaltrackpersonalization.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistrationRequest {

    private String username;
    private String password;

}