package com.nurtdinov.educationaltrackpersonalization.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ApplicationUserPermission {

    EDIT("edit:editor"),
    READ("read:user");

    @Getter
    private final String permission;

}
