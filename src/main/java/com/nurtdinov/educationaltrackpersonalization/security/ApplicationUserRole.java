package com.nurtdinov.educationaltrackpersonalization.security;

import java.util.Set;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ApplicationUserRole {

    USER(Sets.newHashSet(ApplicationUserPermission.READ)),
    EDITOR(Sets.newHashSet(ApplicationUserPermission.EDIT, ApplicationUserPermission.READ));

    @Getter
    private final Set<ApplicationUserPermission> permissions;

}
